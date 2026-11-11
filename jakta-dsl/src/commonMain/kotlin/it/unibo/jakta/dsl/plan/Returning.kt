package it.unibo.jakta.dsl.plan

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanScope
import kotlin.reflect.typeOf

/**
 * A goal plan builder whose result type [R] was declared with `returns<R>()`.
 *
 * Without a declaration, the result type of a plan is inferred from the last expression of its body,
 * which can be narrower than intended: a body ending with `Ok(x)` returns `Ok`, and the plan is not selected
 * for `achieveWithResult<Result>()`, since plan selection does not recognise user-defined subtypes.
 * Declaring `returns<Result>()` widens it, and the compiler checks the body against it.
 */
@JaktaDSL
class Returning<out Builder : PlanBuilder<*, *, *>, R> @PublishedApi internal constructor(
    /**
     * The builder of the plan.
     */
    @PublishedApi internal val builder: Builder,
)

/**
 * Defines the body of a goal addition plan whose result type was declared with `returns<R>()`.
 * The body must return an [R].
 * @return the constructed goal addition plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified R> Returning<PlanBuilder.Addition.Goal<B, G, Ctx>, R>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> R,
): Plan.Goal.Addition<B, G, Ctx, R> where B : Any, G : Any, Ctx : Any =
    builder.triggersImpl(typeOf<R>(), body)

/**
 * Defines the body of a goal removal plan whose result type was declared with `returns<R>()`.
 * The body must return an [R].
 * @return the constructed goal removal plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified R> Returning<PlanBuilder.Removal.Goal<B, G, Ctx>, R>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> R,
): Plan.Goal.Removal<B, G, Ctx, R> where B : Any, G : Any, Ctx : Any =
    builder.triggersImpl(typeOf<R>(), body)

/**
 * Defines the body of a goal failure interception plan whose result type was declared with `returns<R>()`.
 * The body must return an [R]: it replaces the result of the failed goal.
 * @return the constructed goal failure interception plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified R> Returning<PlanBuilder.FailureInterception.Goal<B, G, Ctx>, R>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> R,
): Plan.Goal.Failure<B, G, Ctx, R> where B : Any, G : Any, Ctx : Any =
    builder.triggersImpl(typeOf<R>(), body)
