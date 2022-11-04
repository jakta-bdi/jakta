package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan

internal class AgentImpl(override val context: AgentContext): Agent {
    override fun selectEvent(events: EventQueue) = events.first()
    override fun selectApplicablePlan(plans: Iterable<Plan>) = plans.first()
    override fun scheduleIntention(intentions: IntentionPool) =
        SchedulingResult(intentions.pop(), intentions.nextIntention())
}