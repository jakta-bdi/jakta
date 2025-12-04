package it.unibo.jakta.plan

import it.unibo.jakta.environment.Environment
import kotlin.reflect.KType

/**
 * Implementation of PlanBuilder for belief additions.
 */
class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    private val trigger: Belief.() -> Context?,
    private var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x },
) : PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief>.(Context) -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Env, Context> = this.also { this.guard = guard }

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ): Plan.Belief.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefAdditionPlan, addBeliefPlan)
}

/**
 * Implementation of PlanBuilder for goal additions.
 */
class GoalAdditionPlanBuilderImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x },
) : PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief>.(Context) -> Context?,
    ): PlanBuilder.Addition.Goal<Belief, Goal, Env, Context> = this.also { this.guard = guard }

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ): Plan.Goal.Addition<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalAdditionPlan, addGoalPlan)
}

/**
 * Implementation of PlanBuilder for belief removals.
 */
class BeliefRemovalPlanBuilderImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Env, *, *>) -> Unit,
    private val trigger: Belief.() -> Context?,
    private var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x },
) : PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief>.(Context) -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Env, Context> = this.also { this.guard = guard }

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ): Plan.Belief.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefRemovalPlan, addBeliefPlan)
}

/**
 * Implementation of PlanBuilder for goal removals.
 */
class GoalRemovalPlanBuilderImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x },
) : PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief>.(Context) -> Context?,
    ): PlanBuilder.Removal.Goal<Belief, Goal, Env, Context> = this.also { this.guard = guard }

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ): Plan.Goal.Removal<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalRemovalPlan, addGoalPlan)
}

/**
 * Implementation of PlanBuilder for goal failures.
 */
class GoalFailurePlanBuilderImpl<Belief : Any, Goal : Any, Env : Environment, Context : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Env, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief>.(Context) -> Context? = { x -> x },
) : PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief>.(Context) -> Context?,
    ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Env, Context> = this.also { this.guard = guard }

    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend PlanScope<Belief, Goal, Env, Context>.() -> PlanResult,
    ): Plan.Goal.Failure<Belief, Goal, Env, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalFailurePlan, addGoalPlan)
}

private fun <B, G, E, TE, C, PR, P> buildAndRegisterPlan(
    resultType: KType,
    trigger: TE.() -> C?,
    guard: GuardScope<B>.(C) -> C?,
    body: suspend PlanScope<B, G, E, C>.() -> PR,
    builder: (
        (TE) -> C?,
        GuardScope<B>.(C) -> C?,
        suspend PlanScope<B, G, E, C>.() -> PR,
        KType,
    ) -> P,
    register: (P) -> Unit,
): P where B : Any, G : Any, E : Environment, TE : Any, C : Any, P : Plan<B, G, E, TE, C, PR> =
    builder(trigger, guard, body, resultType).also { register(it) }
