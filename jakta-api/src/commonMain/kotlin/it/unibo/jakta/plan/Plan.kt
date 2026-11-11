package it.unibo.jakta.plan

import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.reflection.isSubtypeOfMultiPlatform
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A *plan* is a recipe that an agent can follow to achieve a goal or as a reaction to a change in its beliefs.
 * A plan is triggered by an event related to a [TriggerEntity] (either a belief or a goal),
 * and it may have a guard condition to check if the plan is applicable in the current context
 * by checking the current beliefs of the agent.
 * Once triggered and applicable, the plan's body is executed
 * allowing the agent to perform actions in its environment to achieve the desired outcome.
 */
sealed interface Plan<Belief : Any, Goal : Any, TriggerEntity : Any, Context : Any, PlanResult> {
    /**
     * The identifier of the plan, fixed when the plan is created: either the label given by the user
     * (see [PlanID.labelled]) or a generated one. Plans are equal when their ids are equal.
     */
    val id: PlanID

    /**
     * Metadata attached to the plan when it was defined, e.g. a priority read by a selection policy.
     * The core does not interpret it.
     */
    val metadata: Map<String, Any>
        get() = emptyMap()

    /**
     * Function to determine if the plan is triggered by the given [TriggerEntity].
     * If the plan is triggered, it returns a [Context] object that can provides additional information
     * about the triggering event; otherwise, it returns null.
     */
    val trigger: (TriggerEntity) -> Context?

    /**
     * The guard condition that must be satisfied for the plan to be applicable given the current beliefs of the agent.
     * It takes a [GuardScope] and the [Context] returned by the trigger function
     * and returns a possibly modified [Context] if the guard condition is met, or null otherwise.
     */
    val guard: GuardScope<Belief, Context>.() -> Context?

    /**
     * The body of the plan, which is a suspend function that defines the actions to be performed
     * when the plan is executed. It operates within a [PlanScope] that provides access
     * to the agent's actions, the environment, and the context of the triggering event.
     */
    val body: suspend context(Context)
    (PlanScope<Belief, Goal, Context>) -> PlanResult

    /**
     * The type of result produced by the plan upon execution.
     */
    val resultType: KType

    /**
     * Returns the context produced by the trigger for the given [TriggerEntity] [e],
     * or null if the plan is not relevant for it:
     * a plan is relevant if its result type is a subtype of [desiredResult]
     * and its trigger returns a non-null context for [e].
     * The trigger is evaluated at most once.
     */
    fun relevantContext(e: TriggerEntity, desiredResult: KType = typeOf<Any?>()): Context? =
        if (resultType.isSubtypeOfMultiPlatform(desiredResult)) trigger(e) else null

    /**
     * Evaluates the guard on the given [beliefs] for a [triggerContext] returned by [relevantContext].
     * @return the [Option] to run, with the context returned by the guard, or null if the guard does not hold.
     */
    fun optionFor(
        beliefs: Collection<Belief>,
        triggerContext: Context,
    ): Option<Belief, Goal, TriggerEntity, Context, PlanResult>? =
        guard(GuardScope(beliefs, triggerContext))?.let { Option(this, it) }

    /**
     * Evaluates the trigger for [e], then the guard on the given [beliefs], once each.
     * @return the [Option] to run if the plan is applicable, null otherwise.
     */
    fun optionFor(
        beliefs: Collection<Belief>,
        e: TriggerEntity,
        desiredResult: KType = typeOf<Any?>(),
    ): Option<Belief, Goal, TriggerEntity, Context, PlanResult>? =
        relevantContext(e, desiredResult)?.let { optionFor(beliefs, it) }

    /**
     * Checks if the plan is relevant for the given [TriggerEntity] and desired result type.
     * A plan is considered relevant if its result type is a subtype of the desired result type
     * and if the trigger function returns a non-null context for the provided entity.
     */
    fun isRelevant(e: TriggerEntity, desiredResult: KType = typeOf<Any?>()): Boolean =
        relevantContext(e, desiredResult) != null

    /**
     * Checks if the plan is applicable in the given [GuardScope] for the provided [TriggerEntity]
     * and desired result type. A plan is considered applicable if it is relevant
     * and if the guard condition is satisfied in the given guard scope
     * returning a non-null context.
     */
    fun isApplicable(beliefs: Collection<Belief>, e: TriggerEntity, desiredResult: KType = typeOf<Any?>()): Boolean =
        optionFor(beliefs, e, desiredResult) != null

    /**
     * Executes the plan within the provided [agent], with the [context] computed by its trigger and guard
     * when the plan was selected (see [Option]).
     * The trigger and the guard are not evaluated again: the plan's body runs within a [PlanScope]
     * built from the agent and this context, and its result is returned.
     */
    suspend fun run(agent: MutableAgentState<Belief, Goal>, context: Context): PlanResult =
        context(context) { body(BasePlanScope(agent, context)) }

    /**
     * Plans that are triggered by changes in beliefs.
     */
    // TODO It does not make sense for Belief Plans to have a PlanResult as it will never be awaited on... right?
    // What are the implication on the overall design? Remove it or bind it as Unit?
    sealed interface Belief<B : Any, G : Any, Context : Any, PlanResult> : Plan<B, G, B, Context, PlanResult> {
        /**
         * Plans that are triggered by the addition of a belief.
         */
        interface Addition<B : Any, G : Any, Context : Any, PlanResult> : Belief<B, G, Context, PlanResult>

        /**
         * Plans that are triggered by the removal of a belief.
         */
        interface Removal<B : Any, G : Any, Context : Any, PlanResult> : Belief<B, G, Context, PlanResult>
    }

    /**
     * Plans that are triggered by changes in goals.
     */
    sealed interface Goal<B : Any, G : Any, Context : Any, PlanResult> : Plan<B, G, G, Context, PlanResult> {

        /**
         * Plans that are triggered by the addition of a goal.
         */
        interface Addition<B : Any, G : Any, Context : Any, PlanResult> : Goal<B, G, Context, PlanResult>

        /**
         * Plans that are triggered by the removal of a goal.
         */
        interface Removal<B : Any, G : Any, Context : Any, PlanResult> : Goal<B, G, Context, PlanResult>

        /**
         * Plans that are triggered by the failure of a goal.
         */
        interface Failure<B : Any, G : Any, Context : Any, PlanResult> : Goal<B, G, Context, PlanResult>
    }
}
