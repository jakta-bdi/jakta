package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.MutableAgentContext
import it.unibo.jakta.context.MutableAgentContextStaticFactory
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import java.util.UUID

internal class AgentImpl(
    override val agentID: AgentID = AgentID(),
    override val name: String = "Agent-" + UUID.randomUUID(),
    override val context: MutableAgentContext<Struct, ASBelief, ASEvent, ASPlan, ASActivationRecord, ASIntention, ASAgentContext> = MutableAgentContextStaticFactory.of(),
) : ASAgent {

    override var tags: Map<String, Any> = emptyMap()

    override val lifecycle: AgentLifecycle<Struct, ASBelief>
        get() = AgentLifecycleImpl(this)

    override fun selectEvent(events: List<ASEvent>) = events.firstOrNull()
    override fun selectApplicablePlan(plans: Iterable<ASPlan>) = plans.firstOrNull()
    override fun scheduleIntention(intentions: ASIntentionPool) = intentions.nextIntention()

    override fun replaceTags(tags: Map<String, Any>): ASAgent {
        this.tags += tags
        return this
    }
}
