package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.impl.AgentImpl
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.reasoning.perception.Perception

interface Agent {

    /** Snapshot of Agent's Actual State */
    val context: AgentContext

    /** Event Selection Function*/
    fun selectEvent(events: EventQueue): Event

    /** Plan Selection Function */
    fun selectApplicablePlan(plans:Iterable<Plan>): Plan

    /** Intention Selection Function */
    fun scheduleIntention(intentions: IntentionPool): SchedulingResult

    companion object {
        fun default(): Agent = AgentImpl(AgentContext.of())
        fun of(
            beliefBase: BeliefBase = BeliefBase.empty(),
            events: EventQueue = emptyList(),
            planLibrary: PlanLibrary = PlanLibrary.empty(),
            perception: Perception = Perception.empty(),
            intentions: IntentionPool = IntentionPool.empty(),
        ): Agent = AgentImpl(AgentContext.of(beliefBase, events, planLibrary, perception, intentions))
    }
}