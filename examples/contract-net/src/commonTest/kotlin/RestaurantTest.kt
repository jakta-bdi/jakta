import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import model.CHEFS
import model.Dish
import model.OrderStatus
import model.Restaurant
import model.Station

class RestaurantTest {
    private val restaurant = Restaurant()

    private fun hire(vararg names: String): List<Kitchen> = names.map { name ->
        CHEFS.single { it.name == name }.let { Kitchen(it.name, it.stations, restaurant) }
    }

    /** Runs the restaurant with [kitchens] while [block] runs, once every chef introduced itself to the waiter. */
    private suspend fun open(background: CoroutineScope, kitchens: List<Kitchen>, block: suspend () -> Unit) {
        background.launch { runRestaurant(restaurant, kitchens) }
        withTimeout(TIMEOUT) {
            restaurant.messages.first { log -> log.count { it.performative == "introduction" } == kitchens.size }
            block()
        }
    }

    /** Waits until the order [id] reached a status that satisfies [reached], and returns it. */
    private suspend fun statusOf(id: Int, reached: (OrderStatus) -> Boolean): OrderStatus =
        restaurant.orders.first { orders ->
            orders.any { it.id == id && reached(it.status) }
        }.single { it.id == id }.status

    /** The chef the order [id] was given to. */
    private suspend fun chefOf(id: Int): String = checkNotNull(statusOf(id) { it.chef() != null }.chef())

    private fun OrderStatus.chef(): String? = when (this) {
        is OrderStatus.Assigned -> chef
        is OrderStatus.Served -> chef
        else -> null
    }

    private suspend fun servedBy(id: Int): String =
        (statusOf(id) { it is OrderStatus.Served } as OrderStatus.Served).chef

    @Test
    fun theSoonestChefGetsTheOrderAndTiesGoByName() = runTest {
        open(backgroundScope, hire("ada", "bruno")) {
            // both idle, both with a stove: a tie, and ada comes first
            restaurant.order(Dish.SOUP)
            assertEquals("ada", chefOf(1))
            // ada has the soup to cook: bruno has the steak ready sooner
            restaurant.order(Dish.STEAK)
            assertEquals("bruno", chefOf(2))
            assertEquals("bruno", servedBy(2))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class) // currentTime
    @Test
    fun aNappingChefDoesNotAnswerAndTheWaiterDecidesAtTheDeadline() = runTest {
        val (ada, bruno) = hire("ada", "bruno")
        bruno.toggleNap()
        open(backgroundScope, listOf(ada, bruno)) {
            val ordered = currentTime
            restaurant.order(Dish.STEAK)
            assertEquals("ada", chefOf(1))
            assertTrue(currentTime - ordered >= DEADLINE.inWholeMilliseconds, "Decided before the deadline")
            bruno.toggleNap()
            // now bruno answers, and is the soonest: ada is still cooking the first steak
            restaurant.order(Dish.STEAK)
            assertEquals("bruno", chefOf(2))
        }
    }

    @Test
    fun aChefLosingTheStationGivesTheOrderBackAndAnotherCooksIt() = runTest {
        val (ada, bruno) = hire("ada", "bruno")
        open(backgroundScope, listOf(ada, bruno)) {
            restaurant.order(Dish.STEAK)
            assertEquals("ada", chefOf(1))
            ada.state.first { it.cooking != null }
            ada.toggleStation(Station.GRILL)
            assertEquals("bruno", servedBy(1))
            assertTrue(restaurant.messages.value.any { it.performative == "failure" && it.sender == "ada" })
        }
    }

    @Test
    fun ordersSpreadAcrossTheChefsAsTheirQueuesGrow() = runTest {
        open(backgroundScope, hire("ada", "bruno", "dara")) {
            repeat(3) { restaurant.order(Dish.STEAK) }
            // each order makes the next bid of its chef later: the steaks go to a different grill each
            assertEquals(listOf("ada", "bruno", "dara"), (1..3).map { chefOf(it) })
            assertEquals(emptyList(), restaurant.messages.value.filter { it.sender in it.receivers })
        }
    }

    @Test
    fun anOrderNobodyCanCookIsOrderedAgainUntilSomeoneCan() = runTest {
        val (bruno) = hire("bruno")
        open(backgroundScope, listOf(bruno)) {
            restaurant.order(Dish.SALAD)
            statusOf(1) { it == OrderStatus.Retrying }
            bruno.toggleStation(Station.CHOPPING_BOARD)
            assertEquals("bruno", servedBy(1))
        }
    }

    @Test
    fun theWaiterTakesBackTheOrdersOfAChefThatFellAsleep() = runTest {
        val (ada, bruno) = hire("ada", "bruno")
        open(backgroundScope, listOf(ada, bruno)) {
            restaurant.order(Dish.STEAK)
            assertEquals("ada", chefOf(1))
            ada.state.first { it.cooking != null }
            ada.toggleNap()
            // ada does not serve the steak in the time it promised: the waiter cancels it, and bruno cooks it
            assertEquals("bruno", servedBy(1))
            assertTrue(restaurant.messages.value.any { it.performative == "cancel" && "ada" in it.receivers })
            assertEquals(emptyList(), ada.state.value.rail)
        }
    }

    private companion object {
        val TIMEOUT = 10.minutes
    }
}
