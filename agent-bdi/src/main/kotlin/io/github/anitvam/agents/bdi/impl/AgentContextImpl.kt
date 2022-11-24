package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.goals.actions.ActionLibrary
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.perception.Perception

/** Implementation of Agent's [AgentContext] */
internal data class AgentContextImpl(
    override val beliefBase: BeliefBase,
    override val events: EventQueue,
    override val planLibrary: PlanLibrary,
    override val perception: Perception,
    override val intentions: IntentionPool,
    override val actionLibrary: ActionLibrary,
) : AgentContext {
    override fun toString(): String {
        return """
            {
                beliefBase = [${beliefBase.forEach { print("$it ") } }]
                events = $events
                planLibrary = [${planLibrary.plans}]
                perception = [$perception]
                intentions = [$intentions]
                actionLibrary = [${actionLibrary.actions}]
            }
        """.trimIndent()
    }
}
