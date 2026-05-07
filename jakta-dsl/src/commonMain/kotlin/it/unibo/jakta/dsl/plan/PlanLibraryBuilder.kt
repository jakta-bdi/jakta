package it.unibo.jakta.dsl.plan

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.plan.Plan

/**
 * Builder interface for defining a plan library with various types of plans.
 */
@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Skills : Any> {

    /**
     * Provides access to builders for adding plans triggered by belief and goal additions.
     */
    val adding: TriggerBuilder.Addition<Belief, Goal, Skills>

    /**
     * Provides access to builders for adding plans triggered by belief and goal removals.
     */
    val removing: TriggerBuilder.Removal<Belief, Goal, Skills>

    /**
     * Provides access to builders for adding plans triggered by goal failure interceptions.
     */
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Skills>

    /**
     * Adds a belief plan to the plan library being constructed.
     * @param plan the belief plan to add
     */
    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>)

    /**
     * Adds a goal plan to the plan library being constructed.
     * @param plan the goal plan to add
     */
    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>)
}
