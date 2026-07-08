package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.plan.Plan

/**
 * The read-only state of an agent.
 *
 * @param Belief The type representing the agent's *beliefs*.
 * @param Goal The type representing the agent's *goals*.
 */
interface AgentState<Belief : Any, Goal : Any> {

    /**
     * The *beliefs* held by the agent.
     */
    val beliefs: Collection<Belief>

    /**
     * The set of [Intention]s being pursued by the agent.
     */
    val intentions: Set<Intention>

    /**
     * The list of [Plan.Belief] available to handle [AgentEvent.Internal.Belief] events.
     */
    val beliefPlans: List<Plan.Belief<Belief, Goal, *, *>>

    /**
     * The list of [Plan.Goal] available to handle [AgentEvent.Internal.Goal] events.
     */
    val goalPlans: List<Plan.Goal<Belief, Goal, *, *>>

    /**
     * Mapping function which defines how to (optionally)
     * convert a [AgentEvent.External.Perception] into an [AgentUpdate].
     */
    val perceptionHandler: AgentState<Belief, Goal>.(AgentEvent.External.Perception) -> AgentUpdate<*>?

    /**
     * Mapping function which defines how to (optionally)
     * convert a [AgentEvent.External.Message] into an [AgentUpdate] .
     */
    val messageHandler: AgentState<Belief, Goal>.(AgentEvent.External.Message<*>) -> AgentUpdate<*>?
}
