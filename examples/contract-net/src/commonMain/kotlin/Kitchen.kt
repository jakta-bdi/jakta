import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.Restaurant
import model.Station
import model.Ticket

/** How long the chef takes to walk from one spot of the kitchen to another, at speed 1. */
val WALK_TIME = 0.8.seconds

/** How often the kitchen moves on: a nap or a missing station is noticed within this time. */
private val TICK = 100.milliseconds

/**
 * The state of a kitchen and of its chef.
 *
 * @property stations the stations the kitchen has now.
 * @property napping whether the chef is asleep: the chef does nothing until woken up.
 * @property chefAt the station where the chef stands, or `null` at the pass, where dishes are served.
 * @property rail the tickets the chef pinned, in the order the chef took them.
 * @property cooking the ticket being cooked, if any.
 * @property progress how much of the [cooking] dish is done, from 0 to 1.
 * @property served how many dishes the chef served.
 */
data class KitchenState(
    val stations: Set<Station>,
    val napping: Boolean = false,
    val chefAt: Station? = null,
    val rail: List<Ticket> = emptyList(),
    val cooking: Ticket? = null,
    val progress: Float = 0f,
    val served: Int = 0,
)

/**
 * A chef's kitchen, which is the chef's body: the user switches its stations on and off and lets the chef nap, and
 * the chef's actions take time on the restaurant's clock, never overlap, and stop while the chef naps.
 *
 * @property chef the name of the chef.
 * @property speed how fast the chef works: 2 is twice as fast as the menu's cooking times.
 * @param stations the stations the kitchen has at the start.
 * @param restaurant the restaurant, whose clock the kitchen works on.
 */
class Kitchen(val chef: String, val speed: Double, stations: Set<Station>, private val restaurant: Restaurant) {
    private val mutableState = MutableStateFlow(KitchenState(stations))
    private val busy = Mutex()

    /**
     * The current state of the kitchen.
     */
    val state: StateFlow<KitchenState> = mutableState.asStateFlow()

    /** Adds or removes [station], as the user clicks it. */
    fun toggleStation(station: Station) = mutableState.update {
        it.copy(stations = if (station in it.stations) it.stations - station else it.stations + station)
    }

    /** Sends the chef to sleep, or wakes the chef up, as the user clicks. */
    fun toggleNap() = mutableState.update { it.copy(napping = !it.napping) }

    /** Pins [ticket] on the rail. */
    fun pin(ticket: Ticket) = mutableState.update { it.copy(rail = it.rail + ticket) }

    /** Takes the ticket of the order [id] off the rail. */
    fun unpin(id: Int) = mutableState.update { it.copy(rail = it.rail.filterNot { ticket -> ticket.id == id }) }

    /**
     * Walks to [station], or to the pass if `null`, and returns when the chef is there.
     */
    suspend fun walkTo(station: Station?) = busy.withLock {
        if (state.value.chefAt != station) {
            mutableState.update { it.copy(chefAt = station) }
            work(WALK_TIME) { true }
        }
    }

    /**
     * Cooks the dish of [ticket] at its station, where the chef must stand, and returns whether it is ready:
     * not if the station was switched off meanwhile.
     */
    suspend fun cook(ticket: Ticket): Boolean = busy.withLock {
        val station = ticket.dish.station
        mutableState.update { it.copy(cooking = ticket, progress = 0f) }
        try {
            work(ticket.dish.cookingTime) { done ->
                mutableState.update { it.copy(progress = done) }
                station in state.value.stations && state.value.chefAt == station
            }
        } finally {
            mutableState.update { it.copy(cooking = null, progress = 0f) }
        }
    }

    /**
     * Serves the dish of the order [id] at the pass, where the chef must stand.
     */
    fun serve(id: Int) = mutableState.update { it.copy(served = it.served + 1) }.also { unpin(id) }

    /** What the chef sees of the kitchen. */
    fun sense(): KitchenPerception = state.value.let { KitchenPerception(it.stations, it.napping) }

    /**
     * Spends [time] of work, at the chef's speed and not while napping, calling [going] with how much is done every
     * tick: it stops early, returning `false`, as soon as [going] does.
     */
    private suspend fun work(time: Duration, going: (Float) -> Boolean): Boolean {
        var done = Duration.ZERO
        while (done < time) {
            restaurant.passTime(TICK)
            if (!state.value.napping) {
                done += TICK * speed
                if (!going((done / time).toFloat().coerceAtMost(1f))) return false
            }
        }
        return true
    }
}
