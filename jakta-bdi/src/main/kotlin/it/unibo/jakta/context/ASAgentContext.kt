package it.unibo.jakta.context

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.impl.ASAgentContextImpl
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct

/**
 * AgentSpeak extension for the general concept of AgentContext.
 */
interface ASAgentContext : AgentContext<Struct, ASBelief> {
    val internalActions: Map<String, InternalAction> // TODO("is this need?")
}

/**
 * Methods that are capable to modify the [AgentContext].
 */
interface ASMutableAgentContext : MutableAgentContext<Struct, ASBelief, ASIntention, ASPlan, ASAgentContext> {
    companion object {
        fun of(
            beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
            events: MutableList<Event> = mutableListOf(),
            planLibrary: MutableCollection<ASPlan> = mutableListOf(),
            internalActions: MutableMap<String, InternalAction> = mutableMapOf(),
        ): ASMutableAgentContext = ASAgentContextImpl(
            beliefBase,
            events,
            planLibrary,
            internalActions,
        )
    }
}
