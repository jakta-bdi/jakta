package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.context.AgentContext
import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan
import java.util.UUID

internal data class AgentImpl(
    override val context: AgentContext,
    override val agentID: AgentID = AgentID(),
    override val name: String = "Agent-" + UUID.randomUUID(),
) : Agent {
    override fun selectEvent(events: EventQueue) = events.firstOrNull()
    override fun selectApplicablePlan(plans: Iterable<Plan>) = plans.firstOrNull()
    override fun scheduleIntention(intentions: IntentionPool) =
        SchedulingResult(intentions.pop(), intentions.nextIntention())
}
