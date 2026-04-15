package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL

/**
 * Builder interface for defining a plan library with various types of plans.
 */
@JaktaDSL
interface PlanLibraryBuilder<Belief : Any, Goal : Any> {

    /**
     * Provides access to builders for adding plans triggered by belief and goal additions.
     */
    val adding: TriggerBuilder.Addition<Belief, Goal>

    /**
     * Provides access to builders for adding plans triggered by belief and goal removals.
     */
    val removing: TriggerBuilder.Removal<Belief, Goal>

    /**
     * Provides access to builders for adding plans triggered by goal failure interceptions.
     */
    val failing: TriggerBuilder.FailureInterception<Belief, Goal>

    /**
     * Adds a belief plan to the plan library being constructed.
     * @param plan the belief plan to add
     */
    fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, *, *>)

    /**
     * Adds a goal plan to the plan library being constructed.
     * @param plan the goal plan to add
     */
    fun addGoalPlan(plan: Plan.Goal<Belief, Goal, *, *>)
}

/**
 * Implementation of the PlanLibraryBuilder interface.
 */
class PlanLibraryBuilderImpl<Belief : Any, Goal : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, *, *>) -> Unit,
) : PlanLibraryBuilder<Belief, Goal> {
    override val adding: TriggerBuilder.Addition<Belief, Goal>
        get() = TriggerAdditionImpl(addBeliefPlan, addGoalPlan)
    override val removing: TriggerBuilder.Removal<Belief, Goal>
        get() = TriggerRemovalImpl(addBeliefPlan, addGoalPlan)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal>
        get() = TriggerFailureInterceptionImpl(addGoalPlan)

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, *, *>) {
        addBeliefPlan(plan)
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, *, *>) {
        addGoalPlan(plan)
    }
}
