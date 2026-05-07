package it.unibo.jakta.dsl.plan

import it.unibo.jakta.plan.Plan

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
