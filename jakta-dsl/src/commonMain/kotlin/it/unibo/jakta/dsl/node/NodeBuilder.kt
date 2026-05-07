package it.unibo.jakta.dsl.node

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeBehavior

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface NodeBuilder<Belief : Any, Goal : Any, Skills : Any, Body : Any, N : Node<Body, Skills>> {

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    fun agent(block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit)

    /**
     * Defines an agent with a specific name using the provided builder block.
     * @return the constructed agent.
     */
    fun agent(name: String, block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit)

//    /**
//     * Adds multiple agents to the MAS.
//     */
//    fun withAgents(vararg agents: Agent<Belief, Goal>)

    fun withBehavior(block: () -> NodeBehavior<Body, Skills>)

    /**
     * Builds and returns the Node instance.
     */
    fun build(): N
}
