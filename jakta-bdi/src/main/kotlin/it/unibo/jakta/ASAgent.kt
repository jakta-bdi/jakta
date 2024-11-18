package it.unibo.jakta

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.EventQueue
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.SchedulingResult
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent : Taggable<ASAgent> {

    val agentID: AgentID

    val name: String

    /** Agent's Actual State */
    val context: ASMutableAgentContext

    /** Event Selection Function*/
    fun selectEvent(events: List<ASEvent>): ASEvent?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<ASPlan>): Plan?

    /** Intention Selection Function */
    fun scheduleIntention(intentions: IntentionPool): SchedulingResult

    fun copy(agentContext: AgentContext = this.context) =
        of(this.agentID, this.name, agentContext.copy())

    fun copy(
        beliefBase: ASBeliefBase = this.context.beliefBase,
        events: EventQueue = this.context.events,
        planLibrary: PlanLibrary = this.context.planLibrary,
        intentions: IntentionPool = this.context.intentions,
        internalActions: Map<String, InternalAction> = this.context.internalActions,
    ) = of(
        this.agentID,
        this.name,
        context.copy(beliefBase, events, planLibrary, intentions, internalActions),
    )

    companion object {
        fun empty(): ASAgent = AgentImpl(AgentContext.of())
        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: ASBeliefBase = ASBeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): ASAgent = AgentImpl(
            AgentContext.of(beliefBase, events, planLibrary, internalActions),
            agentID,
            name,
        )

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            agentContext: AgentContext,
        ): ASAgent = AgentImpl(agentContext, agentID, name)
    }
}
