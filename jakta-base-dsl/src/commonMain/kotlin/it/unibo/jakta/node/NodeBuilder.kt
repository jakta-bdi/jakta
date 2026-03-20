package it.unibo.jakta.node

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.node.LocalNode

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface NodeBuilder<Belief : Any, Goal : Any, Skills : Any, Body : Any, N: Node<Body, Skills>> {

    /**
     * The node that will execute jakta application.
     */
    val node: N

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

    /**
     * Builds and returns the Node instance.
     */
    fun build(): N
}

/**
 * Implementation of the MasBuilder interface.
 */
open class LocalNodeBuilder<Belief : Any, Goal : Any, Skills : Any, Body : Any> :
    NodeBuilder<Belief, Goal, Skills, Body, LocalNode<Body, Skills>> {
    protected val agents = mutableListOf<AgentBuilder<Belief, Goal, Skills, Body>>()
    override val node = LocalNode<Body, Skills>()

    override fun agent(block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit) = buildAgent(null, block)

    override fun agent(name: String, block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit) =
        buildAgent(name, block)

    private fun buildAgent(name: String?, block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit) {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Skills, Body>(name)
        val agent: AgentBuilder<Belief, Goal, Skills, Body> = agentBuilder.apply(block)
        agents += agent
    }

//    override fun withAgents(vararg agents: Agent<Belief, Goal>) {
//        this.agents += agents
//    }

//    override fun environment(block: () -> Env) {
//        environment = block()
//    }

    override fun build(): LocalNode<Body, Skills> {
        // val env = environment ?: error { "Must provide an Environment for the MAS" }
        agents.forEach { node.addAgent(it.build()) }
        return node
    }
}
