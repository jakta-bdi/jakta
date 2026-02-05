package it.unibo.jakta.plan.baseImpl

import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.plan.PlanScope

/**
 * Implementation of PlanScope.
 */
data class BasePlanScope<Belief : Any, Goal : Any, Skills: Any, Context : Any>(
    override val agent: MutableAgentState<Belief, Goal, Skills>,
    override val skills: Skills,
    override val context: Context,
) : PlanScope<Belief, Goal, Skills, Context>
