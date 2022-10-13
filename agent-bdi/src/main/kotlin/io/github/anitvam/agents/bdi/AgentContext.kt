package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.impl.AgentContextImpl
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.reasoning.perception.Perception

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase

    /** [Event]s on which the BDI Agent reacts */
    val events: EventQueue

    /** Component of the Agent that let it perceive the environment */
    val perception: Perception

    /** [Plan]s collection of the BDI Agent */
    val planLibrary : PlanLibrary

    /**
     * Belief Update Function (BUF): Function that updates the current [BeliefBase] of the Agent with its new
     * perceptions from the environment.
     * @return the updated [AgentContext]
     */
    fun buf(perceptions: BeliefBase): AgentContext

    companion object {
        fun of(beliefBase: BeliefBase, events: EventQueue) = AgentContextImpl(beliefBase, events)
    }
}
