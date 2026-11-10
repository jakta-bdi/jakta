import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.Node
import it.unibo.jakta.skills.Skill
import kotlin.time.Duration
import model.OrderStatus
import model.Restaurant
import model.Station
import model.Ticket

/**
 * An agent of the restaurant, as its plans see it: the type of `agent` in a plan body, with its Prolog beliefs and
 * goals. The actions of [KitchenSkill] and [WaiterSkill] extend it.
 */
typealias RestaurantAgent = MutableAgentState<PrologBelief, PrologGoal>

/**
 * The skill of a chef: what its plans can do in the kitchen, the chef's body.
 *
 * @param node the node of the restaurant, which knows the body of each agent.
 */
class KitchenSkill(node: Node<Any>) : Skill<Any>(node) {
    private val RestaurantAgent.kitchen get() = node.agents.getValue(id) as Kitchen

    /** Looks around the kitchen: delivers what the chef sees now, and waits until the chef received it. */
    suspend fun RestaurantAgent.lookAround() {
        val kitchen = kitchen
        val reading = kitchen.sense()
        node.publishEvent(reading) { it === kitchen }
        wait({ event: AgentEvent -> event.takeIf { it === reading } })
    }

    /** Walks to [station], or to the pass if `null`. */
    suspend fun RestaurantAgent.walkTo(station: Station?) = kitchen.walkTo(station)

    /** Cooks the dish of [ticket] at its station, returning whether it is ready. */
    suspend fun RestaurantAgent.cook(ticket: Ticket): Boolean = kitchen.cook(ticket)

    /** Pins [ticket] on the rail. */
    fun RestaurantAgent.pin(ticket: Ticket) = kitchen.pin(ticket)

    /** Takes the ticket of the order [id] off the rail. */
    fun RestaurantAgent.unpin(id: Int) = kitchen.unpin(id)

    /** Serves the dish of the order [id] at the pass. */
    fun RestaurantAgent.serve(id: Int) = kitchen.serve(id)
}

/**
 * The skill of the waiter: what its plans can do in the restaurant, the waiter's body.
 *
 * @param node the node of the restaurant, which knows the body of each agent.
 */
class WaiterSkill(node: Node<Any>) : Skill<Any>(node) {
    private val RestaurantAgent.restaurant get() = node.agents.getValue(id) as Restaurant

    /** Opens the restaurant: from now on, the waiter perceives the orders the customers place. */
    fun RestaurantAgent.open() = restaurant.open()

    /** Updates the [status] of the order [id] for everyone to see. */
    fun RestaurantAgent.track(id: Int, status: OrderStatus) = restaurant.track(id, status)

    /** Returns once [time] passed on the restaurant's clock. */
    suspend fun RestaurantAgent.passTime(time: Duration) = restaurant.passTime(time)

    /** How long [time] on the restaurant's clock lasts in real time, as `wait` timeouts need. */
    fun RestaurantAgent.realTime(time: Duration): Duration = restaurant.realTime(time)
}
