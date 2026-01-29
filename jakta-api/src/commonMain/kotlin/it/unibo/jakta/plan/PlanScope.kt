package it.unibo.jakta.plan

import it.unibo.jakta.agent.MutableAgentState

/**
 * Scope available when defining a Plan body.
 */
interface PlanScope<Belief : Any, Goal : Any, Skills: Any, Context : Any> {
    /**
     * The actions that can be performed on the agent that is executing the plan.
     */
    val agent: MutableAgentState<Belief, Goal, Skills>

    /**
     * The skills this Plan can use.
     */
    val skills: Skills

    /**
     * The context provided by the trigger that activated the plan (and optionally, filtered by the guard).
     */
    val context: Context
}
