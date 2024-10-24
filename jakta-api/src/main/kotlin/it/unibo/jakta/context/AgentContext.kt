package it.unibo.jakta.context

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
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

//    val internalActions: Map<String, Action>
}

abstract class MutableAgentContext<Query, Belief, BB> where
    Query : Any,
    BB : BeliefBase<Query, Belief> {

    val mutableBase: MutableBeliefBase<Query, Belief, BB>
    final override val beliefBase: BB get() = mutableBase.snapshot()
    override val events: MutableList<Event>
    override val planLibrary: MutableCollection<Plan<Query, Belief, BB>>

    abstract fun snapshot(): AgentContext<Query, Belief>

}
