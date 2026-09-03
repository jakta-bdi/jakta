package it.unibo.jakta.skills

import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.PlanScope

/**
 * Basic implementation of [NodeTerminationSkill] that simply calls the node's termination method.
 * Takes the [node] to terminate as input.
 */
class NodeTerminationSkill(node: Node<Any>) : Skill<Any>(node) {
    /**
     * Terminates the node by calling its terminateNode() method.
     * Provides an alternative to directly calling node.terminateNode()
     * to avoid passing around the node reference and rely on the skill context instead.
     */
    fun terminateNode() {
        node.terminateNode()
    }
}

/**
 * DSL function to terminate the node the agent is currently on.
 * This function can be called from within a plan's triggers block.
 */
context(skill: NodeTerminationSkill)
fun PlanScope<*, *, *>.terminateNode() {
    skill.terminateNode()
}
