package it.unibo.jakta.plan

import kotlin.reflect.KType

/**
 * Implementation of Plan for belief additions.
 */
data class BeliefAdditionPlan<Belief : Any, Goal : Any, Context : Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
) : Plan.Belief.Addition<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for belief removals.
 */
data class BeliefRemovalPlan<Belief : Any, Goal : Any, Context : Any, PlanResult>(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
) : Plan.Belief.Removal<Belief, Goal, Context, PlanResult>

/**
 *
 */
data class GoalAdditionPlan<Belief : Any, Goal : Any, Context : Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
) : Plan.Goal.Addition<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for goal removals.
 */
data class GoalRemovalPlan<Belief : Any, Goal : Any, Context : Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
) : Plan.Goal.Removal<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for goal failures.
 */
data class GoalFailurePlan<Belief : Any, Goal : Any, Context : Any, PlanResult>(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
) : Plan.Goal.Failure<Belief, Goal, Context, PlanResult>
