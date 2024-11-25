package it.unibo.jakta.context

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.impl.ASAgentContextImpl
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct

/**
 * AgentSpeak extension for the general concept of AgentContext.
 */
interface ASAgentContext : AgentContext<Struct, ASBelief, ASEvent, ASPlan, ASActivationRecord, ASIntention> {
    val internalActions: Map<String, InternalAction> // TODO("is this need?")
}

/**
 * Methods that are capable to modify the [AgentContext].
 */
typealias ASMutableAgentContext = MutableAgentContext<
    Struct,
    ASBelief,
    ASEvent,
    ASPlan,
    ASActivationRecord,
    ASIntention,
    ASAgentContext
>

object MutableAgentContextStaticFactory {
    fun of(
        beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
        events: MutableList<ASEvent> = mutableListOf(),
        planLibrary: MutableCollection<ASPlan> = mutableListOf(),
        internalActions: MutableMap<String, InternalAction> = mutableMapOf(),
    ): ASMutableAgentContext = ASAgentContextImpl(
        beliefBase,
        events,
        planLibrary,
        internalActions,
    )
}
