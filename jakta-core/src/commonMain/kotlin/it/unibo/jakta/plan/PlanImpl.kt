package it.unibo.jakta.plan

import it.unibo.jakta.InternalJaktaAPI
import kotlin.reflect.KType

/**
 * Base class of the plan implementations: a plan is identified by its [id], not by its lambdas.
 * Plans are created through the DSL, never by hand: their constructors are internal,
 * so that their `resultType` is always the type their body returns.
 * Two plans are equal when they are of the same kind and have the same [id],
 * and a plan is printed as its kind and its [id].
 */
abstract class AbstractPlan {
    /**
     * The identifier of the plan.
     */
    abstract val id: PlanID

    final override fun equals(other: Any?): Boolean =
        this === other || other is AbstractPlan && other::class == this::class && other.id == id

    final override fun hashCode(): Int = id.hashCode()

    final override fun toString(): String = "${this::class.simpleName}($id)"
}

/**
 * Implementation of Plan for belief additions.
 */
@OptIn(InternalJaktaAPI::class)
@ConsistentCopyVisibility
data class BeliefAdditionPlan<Belief : Any, Goal : Any, Context : Any, PlanResult> internal constructor(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
    override val id: PlanID = PlanID(),
    override val metadata: Map<String, Any> = emptyMap(),
) : AbstractPlan(),
    Plan.Belief.Addition<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for belief removals.
 */
@OptIn(InternalJaktaAPI::class)
@ConsistentCopyVisibility
data class BeliefRemovalPlan<Belief : Any, Goal : Any, Context : Any, PlanResult> internal constructor(
    override val trigger: (Belief) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
    override val id: PlanID = PlanID(),
    override val metadata: Map<String, Any> = emptyMap(),
) : AbstractPlan(),
    Plan.Belief.Removal<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for goal additions.
 */
@OptIn(InternalJaktaAPI::class)
@ConsistentCopyVisibility
data class GoalAdditionPlan<Belief : Any, Goal : Any, Context : Any, PlanResult> internal constructor(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
    override val id: PlanID = PlanID(),
    override val metadata: Map<String, Any> = emptyMap(),
) : AbstractPlan(),
    Plan.Goal.Addition<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for goal removals.
 */
@OptIn(InternalJaktaAPI::class)
@ConsistentCopyVisibility
data class GoalRemovalPlan<Belief : Any, Goal : Any, Context : Any, PlanResult> internal constructor(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
    override val id: PlanID = PlanID(),
    override val metadata: Map<String, Any> = emptyMap(),
) : AbstractPlan(),
    Plan.Goal.Removal<Belief, Goal, Context, PlanResult>

/**
 * Implementation of Plan for goal failures.
 */
@OptIn(InternalJaktaAPI::class)
@ConsistentCopyVisibility
data class GoalFailurePlan<Belief : Any, Goal : Any, Context : Any, PlanResult> internal constructor(
    override val trigger: (Goal) -> Context?,
    override val guard: GuardScope<Belief, Context>.() -> Context?,
    override val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult,
    override val resultType: KType,
    override val id: PlanID = PlanID(),
    override val metadata: Map<String, Any> = emptyMap(),
) : AbstractPlan(),
    Plan.Goal.Failure<Belief, Goal, Context, PlanResult>
