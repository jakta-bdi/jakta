package it.unibo.jakta.dsl.plan

import it.unibo.jakta.plan.BeliefAdditionPlan
import it.unibo.jakta.plan.BeliefRemovalPlan
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.GoalFailurePlan
import it.unibo.jakta.plan.GoalRemovalPlan
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanScope
import kotlin.reflect.KType

/**
 * Implementation of PlanBuilder for belief additions.
 */
class BeliefAdditionPlanBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    private val addBeliefPlan: (plan: Plan.Belief<Belief, Goal, Skills, *, *>) -> Unit,
    private val trigger: Belief.() -> Context?,
    private var guard: GuardScope<Belief, Context>.() -> Context? = { this.context },
) : PlanBuilder.Addition.Belief<Belief, Goal, Skills, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief, Context>.() -> Context?,
    ): PlanBuilder.Addition.Belief<Belief, Goal, Skills, Context> = this.also { this.guard = guard }

    @Deprecated("Use triggers instead", replaceWith = ReplaceWith("triggers(body)"), level = DeprecationLevel.ERROR)
    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend context(Context) PlanScope<Belief, Goal, Skills, Context>.() -> PlanResult,
    ): Plan.Belief.Addition<Belief, Goal, Skills, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefAdditionPlan, addBeliefPlan)
}

/**
 * Implementation of PlanBuilder for goal additions.
 */
class GoalAdditionPlanBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    private val addGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief, Context>.() -> Context? = { this.context },
) : PlanBuilder.Addition.Goal<Belief, Goal, Skills, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief, Context>.() -> Context?,
    ): PlanBuilder.Addition.Goal<Belief, Goal, Skills, Context> = this.also { this.guard = guard }

    @Deprecated("Use triggers instead", replaceWith = ReplaceWith("triggers(body)"), level = DeprecationLevel.ERROR)
    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend context(Context) PlanScope<Belief, Goal, Skills, Context>.() -> PlanResult,
    ): Plan.Goal.Addition<Belief, Goal, Skills, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalAdditionPlan, addGoalPlan)
}

/**
 * Implementation of PlanBuilder for belief removals.
 */
class BeliefRemovalPlanBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    private val removeBeliefPlan: (plan: Plan.Belief<Belief, Goal, Skills, *, *>) -> Unit,
    private val trigger: Belief.() -> Context?,
    private var guard: GuardScope<Belief, Context>.() -> Context? = { this.context },
) : PlanBuilder.Removal.Belief<Belief, Goal, Skills, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief, Context>.() -> Context?,
    ): PlanBuilder.Removal.Belief<Belief, Goal, Skills, Context> = this.also { this.guard = guard }

    @Deprecated("Use triggers instead", replaceWith = ReplaceWith("triggers(body)"), level = DeprecationLevel.ERROR)
    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend context(Context) PlanScope<Belief, Goal, Skills, Context>.() -> PlanResult,
    ): Plan.Belief.Removal<Belief, Goal, Skills, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::BeliefRemovalPlan, removeBeliefPlan)
}

/**
 * Implementation of PlanBuilder for goal removals.
 */
class GoalRemovalPlanBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    private val removeGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief, Context>.() -> Context? = { this.context },
) : PlanBuilder.Removal.Goal<Belief, Goal, Skills, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief, Context>.() -> Context?,
    ): PlanBuilder.Removal.Goal<Belief, Goal, Skills, Context> = this.also { this.guard = guard }

    @Deprecated("Use triggers instead", replaceWith = ReplaceWith("triggers(body)"), level = DeprecationLevel.ERROR)
    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend context(Context) PlanScope<Belief, Goal, Skills, Context>.() -> PlanResult,
    ): Plan.Goal.Removal<Belief, Goal, Skills, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalRemovalPlan, removeGoalPlan)
}

/**
 * Implementation of PlanBuilder for goal failures.
 */
class GoalFailurePlanBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Context : Any>(
    private val failingGoalPlan: (plan: Plan.Goal<Belief, Goal, Skills, *, *>) -> Unit,
    private val trigger: Goal.() -> Context?,
    private var guard: GuardScope<Belief, Context>.() -> Context? = { this.context },
) : PlanBuilder.FailureInterception.Goal<Belief, Goal, Skills, Context> {
    override fun onlyWhen(
        guard: GuardScope<Belief, Context>.() -> Context?,
    ): PlanBuilder.FailureInterception.Goal<Belief, Goal, Skills, Context> = this.also { this.guard = guard }

    @Deprecated("Use triggers instead", replaceWith = ReplaceWith("triggers(body)"), level = DeprecationLevel.ERROR)
    override fun <PlanResult> triggersImpl(
        resultType: KType,
        body: suspend context(Context) PlanScope<Belief, Goal, Skills, Context>.() -> PlanResult,
    ): Plan.Goal.Failure<Belief, Goal, Skills, Context, PlanResult> =
        buildAndRegisterPlan(resultType, trigger, guard, body, ::GoalFailurePlan, failingGoalPlan)
}

private fun <B, G, S, TE, C, PR, P> buildAndRegisterPlan(
    resultType: KType,
    trigger: TE.() -> C?,
    guard: GuardScope<B, C>.() -> C?,
    body: suspend context(C) PlanScope<B, G, S, C>.() -> PR,
    builder: (
        (TE) -> C?,
        GuardScope<B, C>.() -> C?,
        suspend context(C)
        PlanScope<B, G, S, C>.() -> PR,
        KType,
    ) -> P,
    register: (P) -> Unit,
): P where B : Any, G : Any, S : Any, TE : Any, C : Any, P : Plan<B, G, S, TE, C, PR> =
    builder(trigger, guard, body, resultType).also { register(it) }
