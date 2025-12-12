package it.unibo.jakta.event

import it.unibo.jakta.intention.Intention
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.CompletableDeferred

/**
 * Implements a Goal Addition event.
 * @param[goal] the goal that has been added.
 * @param[resultType] the type of result expected from the plan that will handle this goal.
 * @param[completion] an optional CompletableDeferred to complete when the goal is achieved.
 * @param[intention] the intention from which the event has been generated, if any.
 */
data class GoalAddEvent<G : Any, PlanResult>(
    override val goal: G,
    override val resultType: KType,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
) : Event.Internal.Goal.Add<G, PlanResult> {
    /**
     * Factory methods for GoalAddEvent.
     */
    companion object {
        /**
         * Creates a GoalAddEvent with no expected result i.e. PlanResult = Unit.
         * The created event will have resultType = Any, as it should match any plan result type as the result
         * will be ignored.
         * @param[goal] the goal that has been added.
         * @return the created GoalAddEvent.
         */
        fun <G : Any> withNoResult(goal: G) = GoalAddEvent<G, Unit>(goal, typeOf<Any>())
    }
}

/**
 * Implements a Goal Removal event.
 * @param[goal] the goal that has been removed.
 * @param[resultType] the type of result expected from the plan that will handle this goal.
 * @param[completion] an optional CompletableDeferred to complete when the plan handling the removal is done.
 * @param[intention] the intention from which the event has been generated, if any.
 */
data class GoalRemoveEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
    override val resultType: KType,
) : Event.Internal.Goal.Remove<G, PlanResult>

/**
 * Implements a Goal Failed event.
 * @param[goal] the goal that has failed.
 * @param[resultType] the type of result expected from the plan that will handle this goal failure.
 * @param[completion] an optional CompletableDeferred to complete when the plan handling the failure is done.
 * @param[intention] the intention from which the event has been generated, if any.
 */
data class GoalFailedEvent<G : Any, PlanResult>(
    override val goal: G,
    override val completion: CompletableDeferred<PlanResult>? = null,
    override val intention: Intention? = null,
    override val resultType: KType,
) : Event.Internal.Goal.Failed<G, PlanResult>

/**
 * Implements a Belief Addition event.
 * @param[belief] the belief that has been added.
 * @param[intention] the intention from which the event has been generated, if any.
 */
data class BeliefAddEvent<B : Any>(override val belief: B, override val intention: Intention? = null) :
    Event.Internal.Belief.Add<B>

/**
 * Implements a Belief Removal event.
 * @param[belief] the belief that has been removed.
 * @param[intention] the intention from which the event has been generated, if any.
 */
data class BeliefRemoveEvent<B : Any>(override val belief: B, override val intention: Intention? = null) :
    Event.Internal.Belief.Remove<B>
