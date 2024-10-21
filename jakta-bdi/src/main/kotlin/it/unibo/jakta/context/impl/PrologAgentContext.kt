package it.unibo.jakta.context.impl

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.PlanLibrary

interface PrologAgentContext<T : Trigger<*>> : AgentContext<PrologBelief, PrologBeliefBase, T> {
    fun copy(
        beliefBase: PrologBeliefBase = this.beliefBase,
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
