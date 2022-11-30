package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.goals.actions.InternalAction
import io.github.anitvam.agents.bdi.goals.actions.InternalActions
import io.github.anitvam.agents.bdi.impl.AgentImpl
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.perception.Perception
import java.util.*

interface Agent {

    val name: String

    /** Snapshot of Agent's Actual State */
    val context: AgentContext

    /** Event Selection Function*/
    fun selectEvent(events: EventQueue): Event?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<Plan>): Plan?

    /** Intention Selection Function */
    fun scheduleIntention(intentions: IntentionPool): SchedulingResult

    fun copy(
        name: String = this.name,
        agentContext: AgentContext = this.context,
    ): Agent = of(name, agentContext.copy())

    companion object {
        fun empty(): Agent = AgentImpl(AgentContext.of())
        fun of(
            name: String = "Agent-" + UUID.randomUUID(),
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            perception: Perception = Perception.empty(),
            intentions: IntentionPool = IntentionPool.empty(),
            internalActions: Map<String, InternalAction> = InternalActions.default(),
        ): Agent = AgentImpl(
            AgentContext.of(beliefBase, events, planLibrary, perception, intentions, internalActions),
            name,
        )

        fun of(
            name: String = "Agent-" + UUID.randomUUID(),
            agentContext: AgentContext,
        ): Agent = AgentImpl(agentContext, name)
    }
}
