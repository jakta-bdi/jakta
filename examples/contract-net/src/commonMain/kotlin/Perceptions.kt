import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import model.Order
import model.Station

/**
 * What a chef sees of the kitchen: the [stations] it has now, and whether the chef is [napping].
 */
data class KitchenPerception(val stations: Set<Station>, val napping: Boolean) : Perception

/**
 * Turns what the chef sees into beliefs, `station(S)` for each station and `napping` while asleep, replacing those
 * of the previous reading: only what changed adds or removes beliefs.
 */
fun handleKitchenPerception(event: KitchenPerception): AgentUpdate<*> = AgentUpdate.Replace.where(
    event.stations.map(::stationBelief) + if (event.napping) listOf(nappingBelief) else emptyList(),
) { belief: PrologBelief -> belief.head.functor == "station" || belief.head == nappingBelief.head }

/**
 * What the waiter sees: a customer placed the [order].
 */
data class OrderPerception(val order: Order) : Perception

/**
 * Turns an order into the waiter's belief `order(Id, D)`.
 */
fun handleOrderPerception(event: OrderPerception): AgentUpdate<*> =
    AgentUpdate.Believe(orderBelief("order", event.order.id, event.order.dish))
