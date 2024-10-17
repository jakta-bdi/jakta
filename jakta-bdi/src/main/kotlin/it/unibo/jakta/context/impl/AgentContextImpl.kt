package it.unibo.jakta.context.impl

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.EventQueue
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.PlanLibrary

/** Implementation of Agent's [AgentContext] */
internal class AgentContextImpl(
    override val beliefBase: PrologBeliefBase,
    override val events: EventQueue,
    override val planLibrary: PlanLibrary,
    override val internalActions: Map<String, InternalAction>,
    override val intentions: IntentionPool = IntentionPool.empty(),
) : AgentContext {
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
