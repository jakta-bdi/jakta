package it.unibo.jakta.context

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.impl.ASAgentContextImpl
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct

typealias ASAgentContext = AgentContext<
    Struct,
    ASBelief,
    ASBeliefBase,
    ASEvent,
    ASPlan,
    ASActivationRecord,
    ASIntention,
    BasicEnvironment
    >


typealias ASMutableAgentContext = MutableAgentContext<
    Struct,
    ASBelief,
    ASBeliefBase,
    ASMutableBeliefBase,
    ASEvent,
    ASPlan,
    ASActivationRecord,
    ASIntention,
    ASAgentContext,
    BasicEnvironment
>

object MutableAgentContextStaticFactory {
    fun of(
        beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
        events: MutableList<ASEvent> = mutableListOf(),
        planLibrary: MutableCollection<ASPlan> = mutableListOf(),
        //internalActions: MutableMap<String, InternalAction> = mutableMapOf(),
        environment: BasicEnvironment
    ): ASMutableAgentContext = ASAgentContextImpl(
        beliefBase,
        events,
        planLibrary,
        //internalActions,
        environment,
    )
}
