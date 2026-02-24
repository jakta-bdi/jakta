package it.unibo.jakta.node

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.node.baseImpl.LocalNode

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface NodeBuilder<Belief : Any, Goal : Any, Skills : Any, Body : AgentBody> {

    /**
     * The node that will execute jakta application.
     */
    val node: Node<Body, Skills>

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    @JaktaDSL
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

//    /**
//     * Defines the environment instance for the MAS.
//     * @param[block] a function that constructs the environment.
//     */
//    fun environment(block: () -> Env)

    /**
     * Builds and returns the MAS instance.
     */
    fun build(): Node<Body, Skills>
}

/**
 * Implementation of the MasBuilder interface.
 */
open class NodeBuilderImpl<Belief : Any, Goal : Any, Skills : Any, Body : AgentBody> :
    NodeBuilder<Belief, Goal, Skills, Body> {
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

    override fun build(): Node<Body, Skills> {
        // val env = environment ?: error { "Must provide an Environment for the MAS" }
        agents.forEach { node.addAgent(it.build()) }
        return node
    }
}
