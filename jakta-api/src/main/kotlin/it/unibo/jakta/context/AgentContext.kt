package it.unibo.jakta.context

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext<Query : Any, Belief> {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase<Query, Belief>

    /**
     * The collection of [Event] that the BDI Agent reacts on.
     *
     * As in Jason, Events are modeled with a FIFO queue. Users can provide an agent-specific event selection function,
     * that handle how an event is chosen from the queue.
     */
    val events: List<Event>

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: Collection<Plan<Query, Belief>>

    val intentions: IntentionPool
}

// interface MutableAgentContext<Query: Any, Belief, out IAgentContext: AgentContext<Query, Belief>> {
//
//    val beliefBase: MutableBeliefBase<Query, Belief, out BeliefBase<Query, Belief>>
//
// //    val beliefBase: BB
// //        get() = mutableBase.snapshot()
//
//    val events: MutableList<Event>
//
//    val planLibrary: MutableCollection<out Plan<Query, Belief>>
//
//    // val intentionPool: MutableIntentionPool
//
//    fun snapshot(): IAgentContext
//
// } // L'ho spostata sotto...
