package it.unibo.jakta.plan

import it.unibo.jakta.agent.MutableAgentState

/**
 * Implementation of PlanScope.
 */
data class BasePlanScope<Belief : Any, Goal : Any, Context : Any>(
    override val agent: MutableAgentState<Belief, Goal>,
    override val context: Context,
) : PlanScope<Belief, Goal, Context>
