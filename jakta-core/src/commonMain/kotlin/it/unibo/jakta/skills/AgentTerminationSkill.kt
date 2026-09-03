package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.node.Node

/**
 * Skill allowing an agent to terminate itself.
 * This can be used to implement a "self-destruct" behavior for the agent,
 * or to allow agents to clean up themselves when they are no longer needed.
 */
class AgentTerminationSkill(node: Node<Any>) : Skill<Any>(node) {
    /**
     * Terminates the agent by removing it from the node.
     */
    fun Agent.terminate() {
        node.removeAgent(id)
    }
}

/**
 * Extension function to terminate an agent using the provided [AgentTerminationSkill].
 * This allows for a more concise syntax when terminating an agent.
 */
context(skill: AgentTerminationSkill)
fun Agent.terminate() {
    with(skill) {
        terminate()
    }
}

/**
 * Helper function to create an [AgentTerminationSkill] and terminate an agent.
 * This allows for a more concise syntax when terminating an agent without explicitly creating a skill instance.
 */
context(node: Node<Any>)
fun Agent.terminate() {
    with(AgentTerminationSkill(node)) {
        terminate()
    }
}
