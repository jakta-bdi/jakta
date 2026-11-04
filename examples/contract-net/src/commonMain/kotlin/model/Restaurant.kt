package model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The stations a kitchen can have: every chef's kitchen has some of them.
 */
enum class Station {
    /** Where salads are made. */
    CHOPPING_BOARD,

    /** Where soups are made. */
    STOVE,

    /** Where steaks are made. */
    GRILL,

    /** Where pizzas are made. */
    OVEN,
}

/**
 * The dishes on the menu, each cooked at one [station] in [cookingTime], for a chef of speed 1.
 */
@Suppress("MagicNumber")
enum class Dish(val station: Station, val cookingTime: Duration) {
    /** A salad. */
    SALAD(Station.CHOPPING_BOARD, 3.seconds),

    /** A steak. */
    STEAK(Station.GRILL, 4.seconds),

    /** A soup. */
    SOUP(Station.STOVE, 5.seconds),

    /** A pizza. */
    PIZZA(Station.OVEN, 6.seconds),
}

/**
 * The slip of an order: the order [id], of a [dish]. The waiter writes one for each order, and chefs pin those they
 * take on their rail.
 */
data class Ticket(val id: Int, val dish: Dish)

/**
 * A chef the restaurant can hire: its [name], how fast it works, and the [stations] of its kitchen at the start.
 *
 * @property speed how fast the chef works: 2 is twice as fast as the menu's cooking times.
 */
data class ChefProfile(val name: String, val speed: Double, val stations: Set<Station>)

/**
 * The chefs, up to four: the more stations, the slower.
 */
val CHEFS = listOf(
    ChefProfile("ada", speed = 0.8, stations = Station.entries.toSet()),
    ChefProfile("bruno", speed = 1.2, stations = setOf(Station.GRILL, Station.STOVE)),
    ChefProfile("chen", speed = 1.0, stations = setOf(Station.CHOPPING_BOARD, Station.OVEN, Station.STOVE)),
    ChefProfile("dara", speed = 1.4, stations = setOf(Station.CHOPPING_BOARD, Station.GRILL)),
)

/**
 * Where an order stands, as the waiter tracks it.
 */
sealed interface OrderStatus {
    /** Taken, not seen by the waiter yet. */
    data object New : OrderStatus

    /** The waiter is asking the chefs who can cook it. */
    data object Asking : OrderStatus

    /** Nobody could cook it: the waiter asks again soon. */
    data object Retrying : OrderStatus

    /** Given to [chef], who said it would be ready in [readyIn]. */
    data class Assigned(val chef: String, val readyIn: Duration) : OrderStatus

    /** Served by [chef]. */
    data class Served(val chef: String) : OrderStatus
}

/**
 * An order of a [dish], numbered by [id].
 *
 * @property status where the order stands.
 */
data class Order(val id: Int, val dish: Dish, val status: OrderStatus = OrderStatus.New)

/**
 * A message between two agents, as the [Restaurant] logs it: [content] from [sender] to [receivers].
 * A message sent to several agents at once is one entry.
 *
 * @property performative the kind of message, such as `cfp` or `propose`.
 */
data class LoggedMessage(val sender: String, val receivers: List<String>, val performative: String, val content: String)

private const val MAX_LOGGED_MESSAGES = 200

/**
 * The restaurant: the orders the customers place, which the waiter perceives, the clock everyone works on, and the
 * log of the messages the agents exchange.
 *
 * @param speed how fast the clock runs: 1 is real time, 2 twice as fast.
 */
class Restaurant(private val speed: () -> Double = { 1.0 }) {
    private val mutableOrders = MutableStateFlow<List<Order>>(emptyList())
    private val mutableMessages = MutableStateFlow<List<LoggedMessage>>(emptyList())
    private val placed = Channel<Order>(Channel.UNLIMITED)
    private val opened = CompletableDeferred<Unit>()

    /**
     * The orders, oldest first.
     */
    val orders: StateFlow<List<Order>> = mutableOrders.asStateFlow()

    /**
     * The most recent messages, oldest first.
     */
    val messages: StateFlow<List<LoggedMessage>> = mutableMessages.asStateFlow()

    /**
     * Returns once [time] passed on the restaurant's clock.
     */
    suspend fun passTime(time: Duration) = delay(realTime(time))

    /**
     * How long [time] on the restaurant's clock lasts in real time.
     */
    fun realTime(time: Duration): Duration = time / speed()

    /**
     * A customer orders [dish].
     */
    fun order(dish: Dish) {
        lateinit var order: Order
        mutableOrders.update { orders -> orders + Order(orders.size + 1, dish).also { order = it } }
        placed.trySend(order)
    }

    /**
     * Opens the restaurant, as the waiter is ready to take the orders.
     */
    fun open() = opened.complete(Unit)

    /**
     * Returns the next order placed, once the restaurant is open and there is one.
     */
    suspend fun nextOrder(): Order {
        opened.await()
        return placed.receive()
    }

    /**
     * Updates the [status] of the order [id].
     */
    fun track(id: Int, status: OrderStatus) = mutableOrders.update { orders ->
        orders.map { if (it.id == id) it.copy(status = status) else it }
    }

    /**
     * Logs that [sender] sent [content] to [receiver], joining it to the previous entry if it was the same message
     * to another agent.
     */
    fun log(sender: String, receiver: String, performative: String, content: String) = mutableMessages.update { log ->
        val last = log.lastOrNull()
        if (last != null && last.sender == sender && last.content == content && receiver !in last.receivers) {
            log.dropLast(1) + last.copy(receivers = last.receivers + receiver)
        } else {
            (log + LoggedMessage(sender, listOf(receiver), performative, content)).takeLast(MAX_LOGGED_MESSAGES)
        }
    }
}
