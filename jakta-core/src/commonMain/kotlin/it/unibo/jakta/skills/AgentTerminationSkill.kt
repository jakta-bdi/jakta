package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.node.Node

/**
 * Skill allowing an agent to terminate itself.
 * This can be used to implement a "self-destruct" behavior for the agent,
 * or to allow agents to clean up themselves when they are no longer needed.
 */
interface AgentTerminationSkill {
    /**
     * Terminates the agent.
     * After this method is called, the agent will be removed from the node
     * and will no longer be active in the system.
     */
    fun Agent.terminate()
}

/**
 * Basic implementation of [AgentTerminationSkill] that simply removes the agent from the node.
 * Takes the [node] the agent is on as input.
 */
class BaseAgentTerminationSkill(val node: Node<*, *>) : AgentTerminationSkill {
    override fun Agent.terminate() {
        node.removeAgent(id)
    }
}
