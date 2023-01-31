package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.context.AgentContext
import io.github.anitvam.agents.bdi.impl.AgentImpl
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.perception.Perception
import it.unibo.tuprolog.utils.Taggable
import java.util.*

interface Agent : Taggable<Agent> {

    val agentID: AgentID

    val name: String

    /** Snapshot of Agent's Actual State */
    val context: AgentContext

    /** Event Selection Function*/
    fun selectEvent(events: EventQueue): Event?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<Plan>): Plan?

    /** Intention Selection Function */
    fun scheduleIntention(intentions: IntentionPool): SchedulingResult

    fun copy(agentContext: AgentContext = this.context) =
        of(this.agentID, this.name, agentContext.copy())

    fun copy(
        beliefBase: BeliefBase = this.context.beliefBase,
        events: EventQueue = this.context.events,
        planLibrary: PlanLibrary = this.context.planLibrary,
        perception: Perception = this.context.perception,
        intentions: IntentionPool = this.context.intentions,
        internalActions: Map<String, InternalAction> = this.context.internalActions,
    ) = of(this.agentID, this.name, beliefBase, events, planLibrary, perception, intentions, internalActions)

    companion object {
        fun empty(): Agent = AgentImpl(AgentContext.of())
        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            perception: Perception = Perception.empty(),
            intentions: IntentionPool = IntentionPool.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): Agent = AgentImpl(
            AgentContext.of(beliefBase, events, planLibrary, perception, intentions, internalActions),
            agentID,
            name,
        )

        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID(),
            agentContext: AgentContext,
        ): Agent = AgentImpl(agentContext, agentID, name)
    }
}
