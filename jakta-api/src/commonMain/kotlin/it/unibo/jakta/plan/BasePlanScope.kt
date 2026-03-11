package it.unibo.jakta.plan

import it.unibo.jakta.agent.MutableAgentState

/**
 * Implementation of PlanScope.
 */
data class BasePlanScope<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    override val agent: MutableAgentState<Belief, Goal, Skills>,
    override val skills: Skills,
    override val context: Context,
) : PlanScope<Belief, Goal, Skills, Context>
