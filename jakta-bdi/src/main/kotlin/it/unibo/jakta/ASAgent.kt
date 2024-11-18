package it.unibo.jakta

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.context.MutableAgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent : Agent<Struct, ASBelief>, Taggable<ASAgent> {

    val asContext: ASMutableAgentContext

    override val context: MutableAgentContext<Struct, ASBelief, AgentContext<Struct, ASBelief>>
        get() = asContext

    /** Event Selection Function*/
    fun selectEvent(events: List<ASEvent>): ASEvent?

    override fun selectEvent(events: List<Event>): Event? = selectEvent(events.filterIsInstance<ASEvent>())

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<ASPlan>): ASPlan?

    override fun selectApplicablePlan(plans: Iterable<Plan<Struct, ASBelief>>): Plan<Struct, ASBelief>? =
        selectApplicablePlan(plans.filterIsInstance<ASPlan>())

    /** Intention Selection Function */
    fun scheduleIntention(intentions: ASIntentionPool): ASIntention?

    override fun scheduleIntention(intentions: IntentionPool<Struct, ASBelief>): Intention<Struct, ASBelief>? =
        if (intentions is ASIntentionPool) scheduleIntention(intentions) else null

    companion object {
        fun empty(): ASAgent = AgentImpl(ASMutableAgentContext.of())

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
            events: MutableList<ASEvent> = mutableListOf(),
            planLibrary: MutableCollection<ASPlan> = mutableListOf(),
            //internalActions: MutableMap<String, InternalAction> = InternalActions.default(), //TODO()
        ): ASAgent = AgentImpl(
            ASMutableAgentContext.of(beliefBase, events, planLibrary),
            agentID,
            name,
        )

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            agentContext: ASMutableAgentContext = ASMutableAgentContext.of(),
        ): ASAgent = AgentImpl(agentContext, agentID, name)
    }
}
