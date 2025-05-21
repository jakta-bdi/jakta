package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.collections.MutableClauseCollection.Companion.emptyQueue
import it.unibo.tuprolog.collections.MutableClauseQueue
import it.unibo.tuprolog.core.Struct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.*

internal class AgentImpl(
    override val intentions: ASIntentionPool,
    override val environment: AgentProcess,
    override val beliefBase: BeliefBase<Struct>,
    override val plans: Collection<Plan<Struct>>,
    override val agentID: AgentID = AgentID(),
    override val name: String = "Agent-" + UUID.randomUUID(),
    override var tags: Map<String, Any> = emptyMap(),
    override val events: Queue<Event.AgentEvent> = ArrayDeque(),
) : ASAgent {

//    override val lifecycle: ASAgentLifecycle
//        get() = AgentLifecycleImpl(this)
//
    override fun selectEvent(events: List<ASEvent>) = events.firstOrNull()
    override fun selectApplicablePlan(plans: Iterable<ASPlan>) = plans.firstOrNull()
    override fun scheduleIntention(intentions: ASIntentionPool) = intentions.nextIntention()

    override fun replaceTags(tags: Map<String, Any>): ASAgent {
        this.tags += tags
        return this
    }

    // TODO("I don't like that this in not a single queue")
    override fun sense(): Event? =
        this.events.poll() ?: this.beliefBase.events.poll() ?: this.environment.events.poll()

    override fun deliberate(event: Event): List<Action> {
        TODO("Not yet implemented")
    }
}
