package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.Event

/**
 * Environment which listens for external events and is capable to forward them to agents.
 * @param perceptionToBeliefMappingFunction mapping function which defines how to convert from a [PerceptionPayload] type to a [Belief] type the information.
 * @param agentFilteringFunction filtering function which potentially selects a subset of agents that will receive the information.
 */
class PerceivingEnvironment<PerceptionPayload : Any, Belief : Any, Goal : Any>(
    val perceptionToBeliefMappingFunction: Any.() -> Belief,
    val agentFilteringFunction: Agent<Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>.(
        Any,
    ) -> Boolean = { true }, // TODO(Maybe not enough)
) : Environment {

    private val perceptionBroker =
        PerceptionBroker<PerceptionPayload, Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>(
            perceptionToBeliefMappingFunction = perceptionToBeliefMappingFunction,
            agentFilteringFunction = agentFilteringFunction,
        )

    /**
     * Shares an [Event.External] to agents sharing this environment instance.
     * @param [Event.External] the event that will be possibly notified to agents.
     */
    fun enqueueExternalEvent(event: Event.External) {
        perceptionBroker.trySend(event) // Safe trySend() invocation since the Channel cannot overflow
    }

    /**
     * The environment listens for the next [Event.External] and informs agents about it.
     * @param agents the [Agent]s that will be informed.
     */
    suspend fun perceive(agents: Set<Agent<Belief, Goal, PerceivingEnvironment<PerceptionPayload, Belief, Goal>>>) {
        perceptionBroker.perceive(agents)
    }
}
