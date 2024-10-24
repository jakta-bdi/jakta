package it.unibo.jakta.context.impl

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.beliefs.PrologMutableBeliefBase
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.PlanLibrary
import it.unibo.tuprolog.core.Struct


data class ASAgentContext(
    override val beliefBase: PrologBeliefBase = PrologMutableBeliefBase.empty().snapshot(),
    override val events: List<ASEvent> = emptyList(),
    override val planLibrary: PlanLibrary = PlanLibrary.empty(),
    override val internalActions: Map<String, InternalAction> = InternalActions.default(),
    override val intentions: IntentionPool = IntentionPool.empty(),
) : AgentContext<Struct, PrologBelief, PrologBeliefBase> {

    override fun toString(): String = """
    AgentContext {
        beliefBase = [$beliefBase]
        events = $events
        planLibrary = [${planLibrary.plans}]
        intentions = [$intentions]
        internalActions = [$internalActions]
    }
    """.trimIndent()
}
