package it.unibo.jakta.dsl.node

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.node.Node

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface NodeBuilder<Body : Any, N : Node<Body>> {

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    fun <Belief : Any, Goal : Any> agent(block: AgentBuilder<Belief, Goal, Body>.() -> Unit)

    /**
     * Defines an agent with a specific name using the provided builder block.
     * @return the constructed agent.
     */
    fun <Belief : Any, Goal : Any> agent(name: String, block: AgentBuilder<Belief, Goal, Body>.() -> Unit)

//    /**
//     * Adds multiple agents to the MAS.
//     */
//    fun withAgents(vararg agents: Agent<Belief, Goal>)

    /**
     * Builds and returns the Node instance.
     */
    fun build(): N
}
