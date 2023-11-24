package it.unibo.jakta.agents.bdi.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.bdi.context.AgentContext
import it.unibo.jakta.agents.bdi.events.EventQueue
import it.unibo.jakta.agents.bdi.intentions.IntentionPool
import it.unibo.jakta.agents.bdi.intentions.SchedulingResult
import it.unibo.jakta.agents.bdi.plans.Plan
import java.util.UUID

internal data class AgentImpl(
    override val context: AgentContext,
    override val agentID: AgentID = AgentID(),
    override val name: String = "Agent-" + UUID.randomUUID(),
    override val tags: Map<String, Any> = emptyMap(),
) : Agent {
    override fun selectEvent(events: EventQueue) = events.firstOrNull()
    override fun selectApplicablePlan(plans: Iterable<Plan>) = plans.firstOrNull()
    override fun scheduleIntention(intentions: IntentionPool) =
        SchedulingResult(intentions.pop(), intentions.nextIntention())

    override fun replaceTags(tags: Map<String, Any>): Agent =
        if (tags != this.tags) {
            copy(tags = tags)
        } else {
            this
        }
}
