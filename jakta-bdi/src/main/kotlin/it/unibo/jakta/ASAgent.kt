package it.unibo.jakta

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.MutableAgentContextStaticFactory
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent :
    Agent<Struct, ASBelief, ASEvent, ASPlan, ASActivationRecord, ASIntention, ASAgentContext>,
    Taggable<ASAgent>
{
    companion object {
        fun empty(): ASAgent = AgentImpl()

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
            events: MutableList<ASEvent> = mutableListOf(),
            planLibrary: MutableCollection<ASPlan> = mutableListOf(),
            //internalActions: MutableMap<String, InternalAction> = InternalActions.default(), //TODO()
        ): ASAgent = AgentImpl(
            agentID,
            name,
            MutableAgentContextStaticFactory.of(beliefBase, events, planLibrary),
        )

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            agentContext: ASMutableAgentContext = MutableAgentContextStaticFactory.of(),
        ): ASAgent = AgentImpl( agentID, name, agentContext)
    }
}
