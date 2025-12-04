package it.unibo.jakta.plan

import it.unibo.jakta.agent.AgentActions
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.reflection.isSubtypeOfMultiPlatform // TODO can we avoid needing this?
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
sealed interface Plan<Belief : Any, Goal : Any, Env : Environment, TriggerEntity : Any, Context : Any, PlanResult> {
    /**
     * The unique identifier of the plan.
     */
    val id: PlanID
        get() = PlanID() // TODO check

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
    val guard: GuardScope<Belief>.(Context) -> Context?

    /**
     * The body of the plan, which is a suspend function that defines the actions to be performed
     * when the plan is executed. It operates within a [PlanScope] that provides access
     * to the agent's actions, the environment, and the context of the triggering event.
     */
    val body: suspend (PlanScope<Belief, Goal, Env, Context>) -> PlanResult

    /**
     * The type of result produced by the plan upon execution.
     */
    val resultType: KType

    /**
     * Checks if the plan is relevant for the given [TriggerEntity] and desired result type.
     * A plan is considered relevant if its result type is a subtype of the desired result type
     * and if the trigger function returns a non-null context for the provided entity.
     */
    fun isRelevant(e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOfMultiPlatform(desiredResult) &&
            this.trigger(e) != null

    /**
     * Checks if the plan is applicable in the given [GuardScope] for the provided [TriggerEntity]
     * and desired result type. A plan is considered applicable if it is relevant
     * and if the guard condition is satisfied in the given guard scope
     * returning a non-null context.
     */
    fun isApplicable(guardScope: GuardScope<Belief>, e: TriggerEntity, desiredResult: KType = typeOf<Any>()): Boolean =
        resultType.isSubtypeOfMultiPlatform(desiredResult) &&
            trigger(e)?.let { guard(guardScope, it) != null } ?: false

    /**
     * @return the Context of this Plan applied to the trigger and the guard.
     * The invocation of this method is supposed to be performed during execution,
     * this implies that this plan has a valid context that can be executed.
     */
    private fun getPlanContext(guardScope: GuardScope<Belief>, e: TriggerEntity): Context = trigger(e)?.also {
        guard(guardScope, it)
    } ?: error { "Execution not possible without a plan context" }

    /**
     * Executes the plan within the provided [agent], [guardScope], [environment], and for the given [entity].
     * It constructs a [PlanScope] using the agent, environment, and the context obtained
     * by applying the trigger and guard functions to the entity.
     * The plan's body is then executed within this scope, and the result is returned.
     */
    suspend fun run(
        agent: AgentActions<Belief, Goal>,
        guardScope: GuardScope<Belief>,
        environment: Env,
        entity: TriggerEntity,
    ): PlanResult = body(
        PlanScopeImpl(
            agent,
            environment,
            getPlanContext(guardScope, entity),
        ),
    )

    /**
     * Plans that are triggered by changes in beliefs.
     */
    // TODO It does not make sense for Belief Plans to have a PlanResult as it will never be awaited on... right?
    // What are the implication on the overall design? Remove it or bind it as Unit?
    sealed interface Belief<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
        Plan<B, G, Env, B, Context, PlanResult> {
        /**
         * Plans that are triggered by the addition of a belief.
         */
        interface Addition<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>

        /**
         * Plans that are triggered by the removal of a belief.
         */
        interface Removal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Belief<B, G, Env, Context, PlanResult>
    }

    /**
     * Plans that are triggered by changes in goals.
     */
    sealed interface Goal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
        Plan<B, G, Env, G, Context, PlanResult> {

        /**
         * Plans that are triggered by the addition of a goal.
         */
        interface Addition<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        /**
         * Plans that are triggered by the removal of a goal.
         */
        interface Removal<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>

        /**
         * Plans that are triggered by the failure of a goal.
         */
        interface Failure<B : Any, G : Any, Env : Environment, Context : Any, PlanResult> :
            Goal<B, G, Env, Context, PlanResult>
    }
}
