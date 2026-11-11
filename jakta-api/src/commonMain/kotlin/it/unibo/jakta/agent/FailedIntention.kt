package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent

/**
 * An intention that failed with nobody left to handle the failure: no failure plan recovered it,
 * and no plan was waiting for its goal (it was an initial goal, a goal pursued with `alsoAchieve`,
 * or a plan reacting to a belief change). It is the scope of the agent's failed-intention handler,
 * set with `handlesFailedIntentions` in the DSL.
 */
interface FailedIntention<Belief : Any, Goal : Any> {
    /**
     * The agent, to react to the failure: e.g. `agent.alsoAchieve(goal)` to pursue the goal again.
     */
    val agent: MutableAgentState<Belief, Goal>

    /**
     * The event that started the failed intention: the failure of its goal, or the change of a belief.
     */
    val trigger: AgentEvent.Internal

    /**
     * The goal the failed intention pursued, i.e. the root goal of the intention, not the deepest sub-goal
     * that failed; null if the intention reacted to a belief change.
     */
    val goal: Goal?

    /**
     * Why the intention failed: the exception of the deepest plan that failed, passed up unchanged
     * as the failure escalated, or the exception explaining why no plan was found.
     */
    val cause: Throwable
}

/**
 * How an agent reacts to a [FailedIntention]: a `suspend` block, run on an intention of its own,
 * that can use the [FailedIntention.agent] like a plan body. A failure inside it is only logged.
 */
typealias FailedIntentionHandler<Belief, Goal> = suspend FailedIntention<Belief, Goal>.() -> Unit
