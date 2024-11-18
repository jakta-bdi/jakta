package it.unibo.jakta.context.impl

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.intentions.IntentionPoolStaticFactory
import it.unibo.jakta.plans.ASPlan

class ASAgentContextImpl(
    private val mutableBeliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
    private val mutableEventList: MutableList<ASEvent> = mutableListOf(),
    private val mutablePlanLibrary: MutableCollection<ASPlan> = mutableListOf(),
    private val mutableInternalActions: MutableMap<String, InternalAction> = mutableMapOf(),
    private var mutableIntentionPool: ASIntentionPool = IntentionPoolStaticFactory.empty(),
) : ASMutableAgentContext, ASAgentContext {

    override val beliefBase
        get() = mutableBeliefBase.snapshot()

    override val events: List<ASEvent>
        get() = mutableEventList.toList()

    override val planLibrary: Collection<ASPlan>
        get() = mutablePlanLibrary.toList()

    override val internalActions: Map<String, InternalAction>
        get() = mutableInternalActions.toMap()

    override val intentions: ASIntentionPool
        get() = mutableIntentionPool

    override fun addBelief(belief: ASBelief): Boolean = mutableBeliefBase.add(belief)

    override fun removeBelief(belief: ASBelief): Boolean = mutableBeliefBase.remove(belief)

    override fun addEvent(event: ASEvent): Boolean = mutableEventList.add(event)

    override fun removeEvent(event: ASEvent): Boolean = mutableEventList.remove(event)

    override fun addPlan(plan: ASPlan): Boolean = mutablePlanLibrary.add(plan)

    override fun removePlan(plan: ASPlan): Boolean = mutablePlanLibrary.remove(plan)

    override fun removeIntention(intention: ASIntention): Boolean {
        mutableIntentionPool = mutableIntentionPool.deleteIntention(intention.id)
        return true
    }

    override fun updateIntention(intention: ASIntention): Boolean {
        mutableIntentionPool = mutableIntentionPool.updateIntention(intention)
        return true
    }

    override fun snapshot(): ASAgentContext = ASAgentContextImpl(
        mutableBeliefBase,
        mutableEventList,
        mutablePlanLibrary,
        mutableInternalActions,
        mutableIntentionPool,
    )

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
