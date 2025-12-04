package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentActions
import it.unibo.jakta.environment.Environment

/**
 * Scope available when defining a Plan body.
 */
interface PlanScope<Belief : Any, Goal : Any, Env : Environment, Context : Any> {
    /**
     * The actions that can be performed on the agent that is executing the plan.
     */
    val agent: AgentActions<Belief, Goal>

    /**
     * The environment in which the agent is operating.
     */
    val environment: Env

    /**
     * The context provided by the trigger that activated the plan (and optionally, filtered by the guard).
     */
    val context: Context
}

/**
 * Implementation of PlanScope.
 */
data class PlanScopeImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    override val agent: AgentActions<Belief, Goal>,
    override val environment: Env,
    override val context: Context,
) : PlanScope<Belief, Goal, Env, Context>
