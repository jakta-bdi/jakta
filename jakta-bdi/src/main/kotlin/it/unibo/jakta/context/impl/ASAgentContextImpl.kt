package it.unibo.jakta.context.impl

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASMutableIntentionPool
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.IntentionPoolStaticFactory
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct

internal class ASAgentContextImpl(
    override val mutableBeliefBase: ASMutableBeliefBase = ASMutableBeliefBase.empty(),
    override val mutableEventList: MutableList<ASEvent> = mutableListOf(),
    override val mutablePlanLibrary: MutableCollection<ASPlan> = mutableListOf(),
    //val mutableInternalActions: MutableMap<String, InternalAction> = mutableMapOf(),
    override val environment: BasicEnvironment,
    override val mutableIntentionPool: ASMutableIntentionPool = IntentionPoolStaticFactory.empty(),
) : ASMutableAgentContext, ASAgentContext {

    override fun snapshot(): ASAgentContext = ASAgentContextImpl(
        mutableBeliefBase,
        mutableEventList,
        mutablePlanLibrary,
        //mutableInternalActions,
        environment,
        mutableIntentionPool,
    )

//    override val internalActions: Map<String, InternalAction>
//        get() = mutableInternalActions.toMap()
    override val beliefBase: ASBeliefBase
        get() = mutableBeliefBase.snapshot()
    override val events: List<ASEvent>
        get() = mutableEventList.toList()
    override val planLibrary: Collection<ASPlan>
        get() = mutablePlanLibrary
    override val intentions: IntentionPool<Struct, ASBelief, ASEvent, ASActivationRecord, ASIntention, ASPlan>
        get() = mutableIntentionPool

    override fun toString(): String = """
    AgentContext {
        beliefBase = [$beliefBase]
        events = $events
        planLibrary = [$planLibrary]
        intentions = [$intentions]
    }
    """.trimIndent()
}
