package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.EventQueue
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.SchedulingResult
import it.unibo.jakta.plans.Plan
import java.util.UUID

internal data class AgentImpl(
    override val context: AgentContext,
    override val agentID: AgentID = AgentID(),
    override val name: String = "Agent-" + UUID.randomUUID(),
    override val tags: Map<String, Any> = emptyMap(),
) : ASAgent {
    override fun selectEvent(events: EventQueue) = events.firstOrNull()
    override fun selectApplicablePlan(plans: Iterable<Plan>) = plans.firstOrNull()
    override fun scheduleIntention(intentions: IntentionPool) =
        SchedulingResult(intentions.pop(), intentions.nextIntention())

    override fun replaceTags(tags: Map<String, Any>): ASAgent =
        if (tags != this.tags) {
            copy(tags = tags)
        } else {
            this
        }
}
