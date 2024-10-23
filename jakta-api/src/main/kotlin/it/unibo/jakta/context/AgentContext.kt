package it.unibo.jakta.context

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext<Query, Belief, BB, T> where
      Query : Any,
      BB : BeliefBase<Query, Belief, BB>,
       {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BB

    /**
     * The collection of [Event] that the BDI Agent reacts on.
     *
     * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
     * that handle how an event is chosen from the queue.
     */
    val events: List<Event<T>>

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: PlanLibrary

    val intentions: IntentionPool

    val internalActions: Map<String, InternalAction>
}
