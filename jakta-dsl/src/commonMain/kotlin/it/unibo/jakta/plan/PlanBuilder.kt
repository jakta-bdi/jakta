package it.unibo.jakta.plan

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import it.unibo.jakta.plan.PlanScope
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Builder interface for defining plans triggered by various events.
 */
@JaktaDSL
sealed interface PlanBuilder<B : Any, G : Any, Env : Environment, Context : Any> {

    /**
     * Builder interface for defining plans triggered by additions of beliefs or goals.
     */
    sealed interface Addition<B : Any, G : Any, Env : Environment, Context : Any> : PlanBuilder<B, G, Env, Context> {

        /**
         * Builder interface for defining plans triggered by additions of beliefs.
         */
        interface Belief<B : Any, G : Any, Env : Environment, Context : Any> : Addition<B, G, Env, Context> {

            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Belief<B, G, Env, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed belief addition plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Belief.Addition<B, G, Env, Context, PlanResult>
        }

        /**
         * Builder interface for defining plans triggered by additions of goals.
         */
        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> : Addition<B, G, Env, Context> {

            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal addition plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Addition<B, G, Env, Context, PlanResult>
        }
    }

    /**
     * Builder interface for defining plans triggered by removals of beliefs or goals.
     */
    sealed interface Removal<B : Any, G : Any, Env : Environment, Context : Any> : PlanBuilder<B, G, Env, Context> {

        /**
         * Builder interface for defining plans triggered by removals of beliefs.
         */
        interface Belief<B : Any, G : Any, Env : Environment, Context : Any> : Removal<B, G, Env, Context> {
            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Belief<B, G, Env, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed belief removal plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Belief.Removal<B, G, Env, Context, PlanResult>
        }

        /**
         * Builder interface for defining plans triggered by removals of goals.
         */
        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> : Removal<B, G, Env, Context> {
            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal removal plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Removal<B, G, Env, Context, PlanResult>
        }
    }

    /**
     * Builder interface for defining plans triggered by failure interceptions.
     */
    sealed interface FailureInterception<B : Any, G : Any, Env : Environment, Context : Any> :
        PlanBuilder<B, G, Env, Context> {
        // TODO should we add Belief failure interception??
        /**
         * Builder interface for defining plans triggered by goal failure interceptions.
         */
        interface Goal<B : Any, G : Any, Env : Environment, Context : Any> :
            FailureInterception<B, G, Env, Context> {
            /**
             * Adds a guard condition to the plan that must be satisfied for the plan to trigger.
             * @param guard a function that takes the context and returns a modified context or null to block the plan
             * @return the updated plan builder with the guard applied
             */
            infix fun onlyWhen(guard: GuardScope<B>.(Context) -> Context?): Goal<B, G, Env, Context>

            /**
             * Defines the body of the plan to be executed when triggered.
             * @param body a suspend function that defines the plan's actions within the PlanScope
             * @param resultType the type of result produced by the plan
             * @return the constructed goal failure interception plan
             */
            @Deprecated("Use triggers instead", ReplaceWith("triggers(body)"), DeprecationLevel.ERROR)
            fun <PlanResult> triggersImpl(
                resultType: KType,
                body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
            ): Plan.Goal.Failure<B, G, Env, Context, PlanResult>
        }
    }
}

/**
 * Defines the body of a belief addition plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed belief addition plan
 */
@Suppress("DEPRECATION_ERROR")
inline infix fun <B, G, Env, Context, reified PlanResult> PlanBuilder.Addition.Belief<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Belief.Addition<B, G, Env, Context, PlanResult> where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal addition plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal addition plan
 */
@Suppress("DEPRECATION_ERROR")
inline infix fun <B, G, Env, Context, reified PlanResult> PlanBuilder.Addition.Goal<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Addition<B, G, Env, Context, PlanResult> where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a belief removal plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed belief removal plan
 */
@Suppress("DEPRECATION_ERROR")
inline infix fun <B, G, Env, Context, reified PlanResult> PlanBuilder.Removal.Belief<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Belief.Removal<B, G, Env, Context, PlanResult> where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal removal plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal removal plan
 */
@Suppress("DEPRECATION_ERROR")
inline infix fun <B, G, Env, Context, reified PlanResult> PlanBuilder.Removal.Goal<B, G, Env, Context>.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Removal<B, G, Env, Context, PlanResult> where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)

/**
 * Defines the body of a goal failure interception plan using a reified type for the result.
 * @param body a suspend function that defines the plan's actions within the PlanScope
 * @return the constructed goal failure interception plan
 */
@Suppress("DEPRECATION_ERROR")
inline infix fun <
    B,
    G,
    Env,
    Context,
    reified PlanResult,
    > PlanBuilder.FailureInterception.Goal<
    B,
    G,
    Env,
    Context,
    >.triggers(
    noinline body: suspend PlanScope<B, G, Env, Context>.() -> PlanResult,
): Plan.Goal.Failure<B, G, Env, Context, PlanResult>
    where B : Any, G : Any, Env : Environment, Context : Any =
    this.triggersImpl(typeOf<PlanResult>(), body)
