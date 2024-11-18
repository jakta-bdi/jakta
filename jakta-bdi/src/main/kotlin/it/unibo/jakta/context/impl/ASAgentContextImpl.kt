package it.unibo.jakta.context.impl

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.ASPlan

data class ASAgentContextImpl(
    private val mutableBeliefBase: ASMutableBeliefBase,
    private val mutableEventList: MutableList<Event>,
    private val mutablePlanLibrary: MutableCollection<ASPlan>,
    private val mutableInternalActions: MutableMap<String, InternalAction>,
    private var mutableIntentionPool: ASIntentionPool = ASIntentionPool.empty(),
) : ASMutableAgentContext, ASAgentContext {

    override val beliefBase
        get() = mutableBeliefBase.snapshot()

    override val events: List<Event>
        get() = mutableEventList.toList()

    override val planLibrary: Collection<ASPlan>
        get() = mutablePlanLibrary.toList()

    override val internalActions: Map<String, InternalAction>
        get() = mutableInternalActions.toMap()

    override val intentions: ASIntentionPool
        get() = mutableIntentionPool

    override fun addBelief(belief: ASBelief): Boolean = mutableBeliefBase.add(belief)

    override fun removeBelief(belief: ASBelief): Boolean = mutableBeliefBase.remove(belief)

    override fun addEvent(event: Event): Boolean =
        if (event is ASEvent) mutableEventList.add(event) else false

    override fun removeEvent(event: Event): Boolean =
        if (event is ASEvent) mutableEventList.remove(event) else false

    override fun addPlan(plan: ASPlan): Boolean = mutablePlanLibrary.add(plan)

    override fun removePlan(plan: ASPlan): Boolean = mutablePlanLibrary.remove(plan)

    override fun removeIntention(intention: ASIntention): Boolean {
        mutableIntentionPool = mutableIntentionPool.deleteIntention(intention.id) as ASIntentionPool
        return true
    }

    override fun updateIntention(intention: ASIntention): Boolean {
        mutableIntentionPool = mutableIntentionPool.updateIntention(intention) as ASIntentionPool
        return true
    }

    override fun snapshot(): ASAgentContext = this.copy()

    override fun toString(): String = """
    AgentContext {
        beliefBase = [$beliefBase]
        events = $events
        planLibrary = [$planLibrary]
        intentions = [$intentions]
        internalActions = [$internalActions]
    }
    """.trimIndent()
}
