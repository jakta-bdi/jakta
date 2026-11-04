import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.skills.MessagingSkill
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.Restaurant

/**
 * Runs the [restaurant], with a waiter and a chef in each of the [kitchens], until cancelled. Each agent runs on its
 * own node, as if each worked on its own machine: they only meet through messages. The waiter perceives the orders,
 * and each chef what changes in its kitchen.
 */
suspend fun runRestaurant(restaurant: Restaurant, kitchens: List<Kitchen>): Unit = coroutineScope {
    // an ID is equal only to itself (it holds a random UUID): the waiter's is created once, and shared with the chefs
    val waiterId = BaseAgentID("waiter")
    val dining = node(NodeBuilders.baseNode<Any>()) {
        context(MessagingSkill(node), WaiterSkill(node)) { withAgents(waiter(waiterId, restaurant)) }
    }
    launch {
        while (true) {
            val order = restaurant.nextOrder()
            dining.publishEvent(OrderPerception(order))
        }
    }
    val kitchenNodes = kitchens.map { kitchen ->
        val node = node(NodeBuilders.baseNode<Any>()) {
            context(MessagingSkill(node), KitchenSkill(node)) {
                withAgents(chef(BaseAgentID(kitchen.chef), kitchen, waiterId, restaurant))
            }
        }
        launch {
            kitchen.state.map { it.stations to it.napping }.distinctUntilChanged().collect {
                node.publishEvent(kitchen.sense())
            }
        }
        node
    }
    mas(NodeBuilders.baseNode()) { withNodes(dining, *kitchenNodes.toTypedArray()) }
        .run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
