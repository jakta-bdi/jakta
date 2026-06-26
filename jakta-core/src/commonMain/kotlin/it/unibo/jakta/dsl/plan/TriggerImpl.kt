package it.unibo.jakta.dsl.plan

import it.unibo.jakta.plan.Plan

/**
 * Implementation of the TriggerBuilder for belief additions and goal additions.
 */
class TriggerAdditionImpl<Belief : Any, Goal : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, *, *>) -> Unit,

) : TriggerBuilder.Addition<Belief, Goal> {

    override fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Context> =
        BeliefAdditionPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.Addition.Goal<Belief, Goal, Context> = GoalAdditionPlanBuilderImpl(addGoalPlan, goalQuery)
}

/**
 * Implementation of the TriggerBuilder for belief removals and goal removals.
 */
class TriggerRemovalImpl<Belief : Any, Goal : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, *, *>) -> Unit,
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, *, *>) -> Unit,
) : TriggerBuilder.Removal<Belief, Goal> {

    override fun <Context : Any> belief(
        beliefQuery: Belief.() -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Context> =
        BeliefRemovalPlanBuilderImpl(addBeliefPlan, beliefQuery)

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.Removal.Goal<Belief, Goal, Context> = GoalRemovalPlanBuilderImpl(addGoalPlan, goalQuery)
}

/**
 * Implementation of the TriggerBuilder for goal failure interceptions.
 */
class TriggerFailureInterceptionImpl<Belief : Any, Goal : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, *, *>) -> Unit,
) : TriggerBuilder.FailureInterception<Belief, Goal> {

    override fun <Context : Any> goal(
        goalQuery: Goal.() -> Context?,
    ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Context> =
        GoalFailurePlanBuilderImpl(addGoalPlan, goalQuery)
}
