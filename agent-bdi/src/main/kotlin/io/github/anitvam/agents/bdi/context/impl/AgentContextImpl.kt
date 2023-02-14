package io.github.anitvam.agents.bdi.context.impl

import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.context.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary

/** Implementation of Agent's [AgentContext] */
internal class AgentContextImpl(
    override val beliefBase: BeliefBase,
    override val events: EventQueue,
    override val planLibrary: PlanLibrary,
    override val internalActions: Map<String, InternalAction>,
    override val intentions: IntentionPool = IntentionPool.empty(),
) : AgentContext {
    override fun toString(): String {
        return """
            {
                beliefBase = [$beliefBase]
                events = $events
                planLibrary = [${planLibrary.plans}]
                intentions = [$intentions]
            }
        """.trimIndent()
    }
}
