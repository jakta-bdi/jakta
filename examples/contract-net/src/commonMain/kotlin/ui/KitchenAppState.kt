package ui

import Kitchen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import model.CHEFS
import model.Dish
import model.Restaurant
import runRestaurant

private const val DEFAULT_CHEFS = 4
private const val DEFAULT_SPEED = 1.0

/**
 * The most chefs the restaurant hires.
 */
val MAX_CHEFS = CHEFS.size

/**
 * State holder for the Kitchen application: the restaurant, the chefs' kitchens, and their run.
 *
 * @property agentDispatcher where the agents run, off the UI thread on desktop.
 */
class KitchenAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        AgentTrace.install()
    }

    /**
     * How fast the restaurant's clock goes: 1 is the normal speed.
     */
    var speed by mutableStateOf(DEFAULT_SPEED)

    /**
     * How many chefs work; changing it resets the restaurant.
     */
    var chefCount by mutableStateOf(DEFAULT_CHEFS)
        private set

    /**
     * The restaurant.
     */
    var restaurant by mutableStateOf(Restaurant { speed })
        private set

    /**
     * The kitchens of the chefs.
     */
    var kitchens by mutableStateOf(hireChefs())
        private set

    /**
     * Whether the restaurant is open.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var job: Job? = null

    private fun hireChefs(): List<Kitchen> = CHEFS.take(chefCount).map { Kitchen(it.name, it.stations, restaurant) }

    /**
     * Closes the restaurant and starts over, with no orders and the chefs' kitchens as at the start.
     */
    fun reset() {
        job?.cancel()
        job = null
        isRunning = false
        restaurant = Restaurant { speed }
        kitchens = hireChefs()
    }

    /**
     * Sets how many chefs work, resetting the restaurant.
     */
    fun changeChefCount(count: Int) {
        chefCount = count.coerceIn(1, MAX_CHEFS)
        reset()
    }

    /**
     * A customer orders [dish]: the waiter takes it once the restaurant is open.
     */
    fun order(dish: Dish) = restaurant.order(dish)

    /**
     * Opens the restaurant: the waiter and the chefs work until reset.
     */
    fun start(scope: CoroutineScope) {
        if (isRunning) return
        val (currentRestaurant, currentKitchens) = restaurant to kitchens
        AgentTrace.clear()
        isRunning = true
        val newJob = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                runRestaurant(currentRestaurant, currentKitchens)
            } finally {
                // a stopped run must not flag a newer one as finished
                if (job === coroutineContext.job) isRunning = false
            }
        }
        job = newJob
        newJob.start()
    }
}
