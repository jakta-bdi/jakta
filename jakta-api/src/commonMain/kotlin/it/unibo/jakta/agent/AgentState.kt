package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan

/**
 * The read-only state of an agent.
 *
 * @param Belief The type representing the agent's *beliefs*.
 * @param Goal The type representing the agent's *goals*.
 * @param Skills The type representing the agent's *skills*.
 */
interface AgentState<Belief: Any, Goal: Any, Skills: Any> : GuardScope<Belief> {

    /**
     * The *beliefs* held by the agent.
     */
    override val beliefs: Collection<Belief>

    /**
     * The set of [Intention]s being pursued by the agent.
     */
    val intentions: Set<Intention>

    /**
     * The list of [Plan.Belief] available to handle [AgentEvent.Internal.Belief] events.
     */
    val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>

    /**
     * The list of [Plan.Goal] available to handle [AgentEvent.Internal.Goal] events.
     */
    val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>

    /**
     * Mapping function which defines how to (optionally) convert a [AgentEvent.External.Perception] into a [AgentEvent.Internal].
     */
    val perceptionHandler: (AgentEvent.External.Perception) -> AgentEvent.Internal?

    /**
     * Mapping function which defines how to (optionally) convert a [AgentEvent.External.Message] into a [AgentEvent.Internal].
     */
    val messageHandler: (AgentEvent.External.Message) -> AgentEvent.Internal?

    /**
     * The [Skills] the agent is allowed to use.
     */
    val skills: Skills
}

