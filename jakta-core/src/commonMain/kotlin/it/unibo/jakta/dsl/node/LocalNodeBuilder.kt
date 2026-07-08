package it.unibo.jakta.dsl.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.agent.AgentBuilderImpl
import it.unibo.jakta.node.LocalNode
import it.unibo.jakta.node.Node

/**
 * Implementation of the MasBuilder interface.
 */
open class LocalNodeBuilder<Body : Any> : NodeBuilder<Body, LocalNode<Body>> {

    override val node: Node<Body>
        get() = _node

    private val _node = LocalNode<Body>()

    protected val agents = mutableListOf<AgentBuilder<*, *, Body>>()

    override fun <Belief : Any, Goal : Any> agent(block: AgentBuilder<Belief, Goal, Body>.() -> Unit) =
        buildAgent(null, block)

    override fun <Belief : Any, Goal : Any> agent(id: AgentID, block: AgentBuilder<Belief, Goal, Body>.() -> Unit) =
        buildAgent(id, block)

    private fun <Belief : Any, Goal : Any> buildAgent(
        id: AgentID?,
        block: AgentBuilder<Belief, Goal, Body>.() -> Unit,
    ) {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Body>(id)
        val agent: AgentBuilder<Belief, Goal, Body> = agentBuilder.apply(block)
        agents += agent
    }

//    override fun withAgents(vararg agents: Agent<Belief, Goal>) {
//        this.agents += agents
//    }

//    override fun environment(block: () -> Env) {
//        environment = block()
//    }

    override fun build(): LocalNode<Body> {
        // val env = environment ?: error { "Must provide an Environment for the MAS" }
        agents.forEach { node.addAgent(it.build(node)) }
        return _node
    }
}
