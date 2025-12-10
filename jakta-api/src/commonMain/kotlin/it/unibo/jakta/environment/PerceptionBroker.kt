package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * A queue of [Event.External].
 * @param perceptionToBeliefMappingFunction mapping function which defines how to convert from a [PerceptionPayload] type to a [Belief] type the information.
 * @param agentFilteringFunction filtering function which potentially selects a subset of agents that will receive the information.
 */
class PerceptionBroker<PerceptionPayload : Any, Belief : Any, Goal : Any, Env : Environment>(
    private val channel: Channel<Event.External> = Channel(UNLIMITED),
    val perceptionToBeliefMappingFunction: PerceptionPayload.() -> Belief,
    val agentFilteringFunction: Agent<Belief, Goal, Env>.(PerceptionPayload) -> Boolean = {
        true
    }, // TODO(Maybe not enough)
) : SendChannel<Event.External> by channel {

    suspend fun perceive(agents: Set<Agent<Belief, Goal, Env>>) {
        coroutineScope {
            // Inherits the coroutine scope from the outer scope
            launch {
                val event = channel.receive()
                when (event) {
                    is Event.External.Message -> TODO()
                    is Event.External.Perception<*> -> {
                        val p: PerceptionPayload = event.payload as PerceptionPayload
                        agents.filter { it.agentFilteringFunction(p) }.forEach {
                            it.trySend(BeliefAddEvent(perceptionToBeliefMappingFunction(p)))
                        }
                    }
                    else -> error("Unrecognized event: $event")
                }
            }
        }
    }
}
