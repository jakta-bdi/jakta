package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment

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

/**
 * Implementation of the PlanLibraryBuilder interface.
 */
class PlanLibraryBuilderImpl<Belief : Any, Goal : Any, Skills : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Skills, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
) : PlanLibraryBuilder<Belief, Goal, Skills> {
    override val adding: TriggerBuilder.Addition<Belief, Goal, Skills>
        get() = TriggerAdditionImpl(addBeliefPlan, addGoalPlan)
    override val removing: TriggerBuilder.Removal<Belief, Goal, Skills>
        get() = TriggerRemovalImpl(addBeliefPlan, addGoalPlan)
    override val failing: TriggerBuilder.FailureInterception<Belief, Goal, Skills>
        get() = TriggerFailureInterceptionImpl(addGoalPlan)

    override fun addBeliefPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>) {
        addBeliefPlan(plan)
    }

    override fun addGoalPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>) {
        addGoalPlan(plan)
    }
}
