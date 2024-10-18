package it.unibo.jakta.context

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.context.impl.AgentContextImpl
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventQueue
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext<B : Belief<*>, C : BeliefBase<B, C>, T : Trigger<*>> {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: C

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

    fun copy(
        beliefBase: C = this.beliefBase,
        events: EventQueue = this.events,
        planLibrary: PlanLibrary = this.planLibrary,
        intentions: IntentionPool = this.intentions,
        internalActions: Map<String, InternalAction> = this.internalActions,
    ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions, intentions)

    companion object {
        fun of(
            beliefBase: C = PrologBeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions)
    }
}
