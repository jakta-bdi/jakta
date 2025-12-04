package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment

/**
 * Builder interface for defining a plan library with various types of plans.
 */
@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any, Env : Environment> {

    /**
     * Provides access to builders for adding plans triggered by belief and goal additions.
     */
    val adding: TriggerBuilder.Addition<Belief, Goal, Env>

    /**
     * Provides access to builders for adding plans triggered by belief and goal removals.
     */
    val removing: TriggerBuilder.Removal<Belief, Goal, Env>

    /**
     * Provides access to builders for adding plans triggered by goal failure interceptions.
     */
    val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>

    /**
     * Adds a belief plan to the plan library being constructed.
     * @param plan the belief plan to add
     */
    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>)

    /**
     * Adds a goal plan to the plan library being constructed.
     * @param plan the goal plan to add
     */
    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>)
}

/**
 * Implementation of the PlanLibraryBuilder interface.
 */
class PlanLibraryBuilderImpl<Belief : Any, Goal : Any, Env : Environment>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
) : PlanLibraryBuilder<Belief, Goal, Env> {
    override val adding: TriggerBuilder.Addition<Belief, Goal, Env>
        get() = TriggerAdditionImpl(addBeliefPlan, addGoalPlan)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Env>
        get() = TriggerRemovalImpl(addBeliefPlan, addGoalPlan)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Env>
        get() = TriggerFailureInterceptionImpl(addGoalPlan)

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Env, *, *>) {
        addBeliefPlan(plan)
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Env, *, *>) {
        addGoalPlan(plan)
    }
}
