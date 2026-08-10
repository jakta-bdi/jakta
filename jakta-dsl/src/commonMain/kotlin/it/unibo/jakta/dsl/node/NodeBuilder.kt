package it.unibo.jakta.dsl.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
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
     * The node instance being built by adding the agents to run on it.
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
     * Add multiple agents to the node using the provided agent factories.
     * Each factory is a function that takes a Node<Body> and returns an AgentSpecification.
     * @param agentFactories vararg of functions that create AgentSpecifications for the node.
     */
    fun <Belief : Any, Goal : Any> withAgents(
        vararg agentFactories: (Node<Body>) -> AgentSpecification<Belief, Goal, Body>,
    )

    /**
     * Builds and returns the Node instance.
     */
    fun build(): N
}
