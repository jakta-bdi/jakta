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
    fun theSoonestChefGetsTheOrder() = runTest {
        open(backgroundScope, hire("ada", "bruno")) {
            // both idle, both with a stove: either can have it
            restaurant.order(Dish.SOUP)
            val first = chefOf(1)
            // the one with the soup to cook needs longer: the other has the steak ready sooner
            restaurant.order(Dish.STEAK)
            val second = chefOf(2)
            assertEquals(setOf("ada", "bruno"), setOf(first, second))
            assertEquals(second, servedBy(2))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class) // currentTime
    @Test
    fun aNappingChefIsOfflineAndTheWaiterDecidesAtTheDeadline() = runTest {
        val (ada, bruno) = hire("ada", "bruno")
        open(backgroundScope, listOf(ada, bruno)) {
            bruno.toggleNap()
            val ordered = currentTime
            restaurant.order(Dish.STEAK)
            assertEquals("ada", chefOf(1))
            assertTrue(currentTime - ordered >= DEADLINE.inWholeMilliseconds, "Decided before the deadline")
            assertTrue(restaurant.messages.value.any { it.lost && it.performative == "cfp" && "bruno" in it.receivers })
            bruno.toggleNap()
            // bruno is back online, and the soonest: ada is still cooking the first steak
            restaurant.order(Dish.STEAK)
            assertEquals("bruno", chefOf(2))
        }
    }

    @Test
    fun aChefLosingTheStationGivesTheOrderBackAndAnotherCooksIt() = runTest {
        val kitchens = hire("ada", "bruno")
        open(backgroundScope, kitchens) {
            restaurant.order(Dish.STEAK)
            val chef = chefOf(1)
            val (winner, other) = kitchens.sortedByDescending { it.chef == chef }
            winner.state.first { it.cooking != null }
            winner.toggleStation(Station.GRILL)
            assertEquals(other.chef, servedBy(1))
            assertTrue(restaurant.messages.value.any { it.performative == "failure" && it.sender == winner.chef })
        }
    }

    @Test
    fun ordersSpreadAcrossTheChefsAsTheirQueuesGrow() = runTest {
        open(backgroundScope, hire("ada", "bruno", "dara")) {
            repeat(3) { restaurant.order(Dish.STEAK) }
            // each order makes the next bid of its chef later: the steaks go to a different grill each
            assertEquals(setOf("ada", "bruno", "dara"), (1..3).map { chefOf(it) }.toSet())
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
        val kitchens = hire("ada", "bruno")
        open(backgroundScope, kitchens) {
            restaurant.order(Dish.STEAK)
            val chef = chefOf(1)
            val (winner, other) = kitchens.sortedByDescending { it.chef == chef }
            winner.state.first { it.cooking != null }
            winner.toggleNap()
            // the winner does not serve the steak in time: the waiter cancels it, and the other chef cooks it
            assertEquals(other.chef, servedBy(1))
            assertTrue(restaurant.messages.value.any { it.lost && it.performative == "cancel" })
            // offline, the winner never got the cancel: back online, it gives the order back, and the waiter ignores it
            winner.toggleNap()
            winner.state.first { it.rail.isEmpty() }
            restaurant.messages.first { log -> log.any { it.performative == "failure" && it.sender == winner.chef } }
            assertEquals(other.chef, servedBy(1))
        }
    }

    @Test
    fun aChefBackOnlineGivesBackItsOrdersAndTheyAreCookedAnyway() = runTest {
        val kitchens = hire("ada", "bruno")
        open(backgroundScope, kitchens) {
            restaurant.order(Dish.STEAK)
            val chef = chefOf(1)
            val (winner, other) = kitchens.sortedByDescending { it.chef == chef }
            winner.state.first { it.cooking != null }
            winner.toggleNap()
            restaurant.order(Dish.STEAK)
            assertEquals(other.chef, chefOf(2))
            assertTrue(restaurant.messages.value.any { it.lost && winner.chef in it.receivers })
            // back before its promise ran out, the winner gives the first steak back anyway, and it is ordered again
            winner.toggleNap()
            restaurant.messages.first { log -> log.any { it.performative == "failure" && it.sender == winner.chef } }
            servedBy(1)
            servedBy(2)
        }
    }

    @Test
    fun bidsCountTimeNotOrders() = runTest {
        open(backgroundScope, hire("ada", "bruno")) {
            // only ada has an oven; then bruno, idle, is sooner with both burgers
            restaurant.order(Dish.CAKE)
            repeat(2) { restaurant.order(Dish.BURGER) }
            assertEquals(listOf("ada", "bruno", "bruno"), (1..3).map { chefOf(it) })
            // one cake takes longer than two burgers: the steak goes to bruno
            restaurant.order(Dish.STEAK)
            assertEquals("bruno", chefOf(4))
        }
    }

    private companion object {
        val TIMEOUT = 10.minutes
    }
}
