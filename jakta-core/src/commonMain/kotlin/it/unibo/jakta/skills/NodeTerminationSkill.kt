package it.unibo.jakta.skills

import it.unibo.jakta.node.Node

/**
 * Skill allowing an agent to terminate the node it is currently on.
 */
interface NodeTerminationSkill {
    /**
     * Terminates the node the agent is currently on.
     * After this method is called, the node will be stopped
     * along with all its agents.
     */
    fun terminateNode()
}

/**
 * Basic implementation of [NodeTerminationSkill] that simply calls the node's termination method.
 * Takes the [node] to terminate as input.
 */
class BaseNodeTerminationSkill(val node: Node<*, *>) : NodeTerminationSkill {
    override fun terminateNode() {
        node.terminateNode()
    }
}
