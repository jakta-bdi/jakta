package it.unibo.jakta.event
import it.unibo.jakta.intention.Intention
import kotlin.reflect.KType
import kotlinx.coroutines.CompletableDeferred

/**
 * Represents a generic event in the MAS.
 */
sealed interface Event {
    /**
     * Represents an internal event within the agent which can trigger plans.
     */
    sealed interface Internal : Event {
        /**
         * The intention from which the event has been generated, if any.
         */
        val intention: Intention? // TODO needed?

        /**
         * Represents a goal-related event.
         */
        sealed interface Goal<G : Any, PlanResult> : Internal {
            /**
             * The optional CompletableDeferred to be completed when the goal is considered achieved
             * (i.e. the plan handling it is done).
             */
            val completion: CompletableDeferred<PlanResult>?

            /**
             * The goal involved in the event.
             */
            val goal: G

            /**
             * The type of result expected from the plan that will handle this goal.
             */
            val resultType: KType

            /**
             * Generated when a goal is added.
             */
            interface Add<G : Any, PlanResult> : Goal<G, PlanResult>

            /**
             * Generated when a goal is *intentionally* removed.
             */
            interface Remove<G : Any, PlanResult> : Goal<G, PlanResult>

            /**
             * Generated when the pursuit of a goal fails either intentionally or due to an error.
             */
            interface Failed<G : Any, PlanResult> : Goal<G, PlanResult>
        }

        /**
         * Represents a belief-related event.
         */
        sealed interface Belief<B : Any> : Internal {
            /**
             * The belief involved in the event.
             */
            val belief: B

            /**
             * Generated when a belief is added.
             */
            interface Add<B : Any> : Belief<B>

            /**
             * Generated when a belief is *intentionally* removed.
             */
            interface Remove<B : Any> : Belief<B>
        }

        /**
         * Generated when a suspended intention is ready to be resumed,
         * signaling that the agent can proceed with a new step.
         */
        data class Step(override val intention: Intention) : Internal
    }

    // TODO external events
    // interface External : Event
}
