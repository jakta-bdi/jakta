package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.impl.AgentContextImpl

/**
 * The Context is the actual state of a BDI Agent's structures.
 */
interface AgentContext {

    /** [BeliefBase] of the BDI Agent */
    val beliefBase: BeliefBase

    /** [Events] on which the BDI Agent reacts */
    val events: EventQueue

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
