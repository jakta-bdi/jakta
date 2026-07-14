package it.unibo.jakta.dsl.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface NodeBuilder<Body : Any, out N : ExecutableNode<Body>> {

    /**
     * The node instance being built.
     */
    val node: Node<Body>

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    fun <Belief : Any, Goal : Any> agent(block: AgentBuilder<Belief, Goal, Body>.() -> Unit)

    /**
     * Defines an agent with a specific name using the provided builder block.
     * @return the constructed agent.
     */
    fun <Belief : Any, Goal : Any> agent(id: AgentID, block: AgentBuilder<Belief, Goal, Body>.() -> Unit)

    /**
     * Builds and returns the Node instance.
     */
    fun build(): N
}
