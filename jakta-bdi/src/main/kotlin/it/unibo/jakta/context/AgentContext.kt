package it.unibo.jakta.context

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.context.impl.AgentContextImpl
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventQueue
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase

    /** [Event]s on which the BDI Agent reacts */
    val events: EventQueue

    /** [Plan]s collection of the BDI Agent */
    val planLibrary: PlanLibrary

    val intentions: IntentionPool

    val internalActions: Map<String, InternalAction>

    fun copy(
        beliefBase: BeliefBase = this.beliefBase,
        events: EventQueue = this.events,
        planLibrary: PlanLibrary = this.planLibrary,
        intentions: IntentionPool = this.intentions,
        internalActions: Map<String, InternalAction> = this.internalActions,
    ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions, intentions)

    companion object {
        fun of(
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): AgentContext = AgentContextImpl(beliefBase, events, planLibrary, internalActions)
    }
}
