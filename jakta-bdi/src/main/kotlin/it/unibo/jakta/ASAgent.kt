package it.unibo.jakta

import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.impl.AgentImpl
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface ASAgent: Agent<Struct>, Taggable<ASAgent> {
    val intentions: ASIntentionPool

    /** Event Selection Function*/
    fun selectEvent(events: List<ASEvent>): ASEvent?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<ASPlan>): ASPlan?

    /** Intention Selection Function */
    fun scheduleIntention(
        intentions: ASIntentionPool
    ): ASIntention

    companion object {
        fun empty(): ASAgent = AgentImpl()

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
            events: MutableList<ASEvent> = mutableListOf(),
            planLibrary: MutableCollection<ASPlan> = mutableListOf(),
            //internalActions: MutableMap<String, InternalAction> = ExecutionActions.default(), //TODO()
        ): ASAgent = AgentImpl(
            agentID,
            name,
            beliefBase,
                events,
                planLibrary)
        )
    }
}
