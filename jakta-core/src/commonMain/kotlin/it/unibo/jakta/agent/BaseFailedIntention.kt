package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent

/**
 * Default implementation of a [FailedIntention].
 */
internal data class BaseFailedIntention<Belief : Any, Goal : Any>(
    override val agent: MutableAgentState<Belief, Goal>,
    override val trigger: AgentEvent.Internal,
    override val goal: Goal?,
    override val cause: Throwable,
) : FailedIntention<Belief, Goal>
