package io.github.anitvam.agents.bdi.reasoning

import io.github.anitvam.agents.bdi.AgentContext

/** Operations made by a BDI Agent during each iteration of its loop */
interface ReasoningCycle {

    /** Reasoning cycle procedure of a BDI Agent */
    fun performReasoning(agentContext: AgentContext) : AgentContext

}