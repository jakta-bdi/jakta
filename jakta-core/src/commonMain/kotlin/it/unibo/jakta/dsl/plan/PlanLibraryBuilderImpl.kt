package it.unibo.jakta.dsl.plan

import it.unibo.jakta.plan.Plan

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
