package it.unibo.jakta.dsl.plan

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanScope
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Builder interface for defining plans triggered by various events.
 */
@JaktaDSL
interface PlanBuilder<B : Any, G : Any, Context : Any> {

    /**
     * Gives the plan a stable, human-readable [label], used as its [it.unibo.jakta.plan.Plan.id].
     * Prefer the [labelled] DSL function, which keeps the type of the builder.
     */
    fun setLabel(label: String)

    /**
     * Attaches [entries] to the [it.unibo.jakta.plan.Plan.metadata] of the plan.
     * Prefer the [withMetadata] DSL function, which keeps the type of the builder.
     */
    fun addMetadata(entries: Map<String, Any>)

    /**
     * Builder interface for defining plans triggered by additions of beliefs or goals.
     */
    interface Addition<B : Any, G : Any, Context : Any> : PlanBuilder<B, G, Context> {

        /**
         * Builder interface for defining plans triggered by additions of beliefs.
         */
        interface Belief<B : Any, G : Any, Context : Any> : Addition<B, G, Context> {

            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            @JaktaDSL
            infix fun onlyWhen(guard: GuardScope<B, Context>.() -> Context?): Belief<B, G, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed belief addition plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
            ): Plan.Belief.Addition<B, G, Context, PlanResult>
        }

        /**
         * Builder interface for defining plans triggered by additions of goals.
         */
        interface Goal<B : Any, G : Any, Context : Any> : Addition<B, G, Context> {
            /**
             * Declares the result type [R] of the goal addition plan, instead of inferring it from the last expression
             * of its body: use it to widen the result type, e.g. `returns<Result>()` for a body ending with `Ok(x)`.
             * The body passed to `triggers` must then return an [R]. Call it after the guard, before `triggers`.
             */
            fun <R> returns(): Returning<Addition.Goal<B, G, Context>, R> = Returning(this)


            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            @JaktaDSL
            infix fun onlyWhen(guard: GuardScope<B, Context>.() -> Context?): Goal<B, G, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal addition plan
             */
            @Deprecated(
                "Use triggers instead",
                ReplaceWith("triggers(body)"),
                DeprecationLevel.ERROR,
            )
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
            ): Plan.Goal.Addition<B, G, Context, PlanResult>
        }
    }

    /**
     * Builder interface for defining plans triggered by removals of beliefs or goals.
     */
    interface Removal<B : Any, G : Any, Context : Any> : PlanBuilder<B, G, Context> {

        /**
         * Builder interface for defining plans triggered by removals of beliefs.
         */
        interface Belief<B : Any, G : Any, Context : Any> : Removal<B, G, Context> {
            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B, Context>.() -> Context?): Belief<B, G, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed belief removal plan
             */
            @Deprecated(
                "Use triggers instead",
                ReplaceWith("triggers(body)"),
                DeprecationLevel.ERROR,
            )
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
            ): Plan.Belief.Removal<B, G, Context, PlanResult>
        }

        /**
         * Builder interface for defining plans triggered by removals of goals.
         */
        interface Goal<B : Any, G : Any, Context : Any> : Removal<B, G, Context> {
            /**
             * Declares the result type [R] of the goal removal plan, instead of inferring it from the last expression
             * of its body: use it to widen the result type, e.g. `returns<Result>()` for a body ending with `Ok(x)`.
             * The body passed to `triggers` must then return an [R]. Call it after the guard, before `triggers`.
             */
            fun <R> returns(): Returning<Removal.Goal<B, G, Context>, R> = Returning(this)

            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B, Context>.() -> Context?): Goal<B, G, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal removal plan
             */
            @Deprecated(
                "Use triggers instead",
                ReplaceWith("triggers(body)"),
                DeprecationLevel.ERROR,
            )
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
            ): Plan.Goal.Removal<B, G, Context, PlanResult>
        }
    }

    /**
     * Builder interface for defining plans triggered by failure interceptions.
     */
    interface FailureInterception<B : Any, G : Any, Context : Any> : PlanBuilder<B, G, Context> {
        /**
         * Builder interface for defining plans triggered by goal failure interceptions.
         */
        interface Goal<B : Any, G : Any, Context : Any> : FailureInterception<B, G, Context> {
            /**
             * Declares the result type [R] of the goal failure interception plan, instead of inferring it from the last expression
             * of its body: use it to widen the result type, e.g. `returns<Result>()` for a body ending with `Ok(x)`.
             * The body passed to `triggers` must then return an [R]. Call it after the guard, before `triggers`.
             */
            fun <R> returns(): Returning<FailureInterception.Goal<B, G, Context>, R> = Returning(this)

            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B, Context>.() -> Context?): Goal<B, G, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal failure interception plan
             */
            @Deprecated(
                "Use triggers instead",
                ReplaceWith("triggers(body)"),
                DeprecationLevel.ERROR,
            )
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
            ): Plan.Goal.Failure<B, G, Context, PlanResult>
        }
    }
}

/**
 * Defines the body of a belief addition plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed belief addition plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified PlanResult> PlanBuilder.Addition.Belief<B, G, Ctx>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> PlanResult,
): Plan.Belief.Addition<B, G, Ctx, PlanResult> where B : Any, G : Any, Ctx : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal addition plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal addition plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified PlanResult> PlanBuilder.Addition.Goal<B, G, Ctx>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> PlanResult,
): Plan.Goal.Addition<B, G, Ctx, PlanResult> where B : Any, G : Any, Ctx : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a belief removal plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed belief removal plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified PlanResult> PlanBuilder.Removal.Belief<B, G, Ctx>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> PlanResult,
): Plan.Belief.Removal<B, G, Ctx, PlanResult> where B : Any, G : Any, Ctx : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal removal plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal removal plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <B, G, Ctx, reified PlanResult> PlanBuilder.Removal.Goal<B, G, Ctx>.triggers(
    noinline body: suspend context(Ctx) PlanScope<B, G, Ctx>.() -> PlanResult,
): Plan.Goal.Removal<B, G, Ctx, PlanResult> where B : Any, G : Any, Ctx : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal failure interception plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal failure interception plan
 */
@Suppress("DEPRECATION_ERROR")
@JaktaDSL
inline infix fun <
    B,
    G,
    Context,
    reified PlanResult,
    > PlanBuilder.FailureInterception.Goal<
    B,
    G,
    Context,
    >.triggers(
    noinline body: suspend context(Context) PlanScope<B, G, Context>.() -> PlanResult,
): Plan.Goal.Failure<B, G, Context, PlanResult>
    where B : Any, G : Any, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Gives the plan a stable, human-readable [label], used as its id: in logs, and to find the plan again.
 * @return this builder, to go on with `onlyWhen` and `triggers`.
 */
@JaktaDSL
infix fun <P : PlanBuilder<*, *, *>> P.labelled(label: String): P = also { it.setLabel(label) }

/**
 * Attaches metadata [entries] to the plan, e.g. a priority read by a plan selection policy.
 * The core does not interpret them.
 * @return this builder, to go on with `onlyWhen` and `triggers`.
 */
@JaktaDSL
fun <P : PlanBuilder<*, *, *>> P.withMetadata(vararg entries: Pair<String, Any>): P =
    also { it.addMetadata(entries.toMap()) }
