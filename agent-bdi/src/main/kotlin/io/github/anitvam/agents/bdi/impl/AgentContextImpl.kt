package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.context.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.perception.Perception

/** Implementation of Agent's [AgentContext] */
internal class AgentContextImpl(
    override val beliefBase: BeliefBase,
    override val events: EventQueue,
    override val planLibrary: PlanLibrary,
    override val perception: Perception,
    override val intentions: IntentionPool,
    override val internalActions: Map<String, InternalAction>,
) : AgentContext {
    override fun toString(): String {
        return """
            {
                beliefBase = [$beliefBase]
                events = $events
                planLibrary = [${planLibrary.plans}]
                perception = [$perception]
                intentions = [$intentions]
            }
        """.trimIndent()
    }
}
