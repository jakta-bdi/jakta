package it.unibo.jakta.context

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention
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

    val intentions: IntentionPool<Query, Belief>
}

 interface MutableAgentContext<Query: Any, Belief, out IAgentContext: AgentContext<Query, Belief>> {
     // val mutableBeliefBase: MutableBeliefBase<Query, Belief, out BeliefBase<Query, Belief>>
     fun addBelief(belief: Belief): Boolean
     fun removeBelief(belief: Belief): Boolean

     // val mutableEventList: MutableList<Event>
     fun addEvent(event: Event): Boolean
     fun removeEvent(event: Event): Boolean

     // val mutablePlanLibrary: MutableCollection<out Plan<Query, Belief>>
     fun addPlan(plan: Plan<Query, Belief>): Boolean
     fun removePlan(plan: Plan<Query, Belief>): Boolean

     // val intentionPool: MutableIntentionPool
     fun removeIntention(intention: Intention<Query, Belief>): Boolean
     fun updateIntention(intention: Intention<Query, Belief>): Boolean

    fun snapshot(): IAgentContext
 }
