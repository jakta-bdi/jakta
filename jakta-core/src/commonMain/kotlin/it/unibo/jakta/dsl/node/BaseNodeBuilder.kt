package it.unibo.jakta.dsl.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.agent.AgentBuilderImpl
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node

/**
 * Implementation of the [NodeBuilder] interface.
 * @param initialNode the initial [ExecutableNode].
 */
open class BaseNodeBuilder<Body : Any, out N : ExecutableNode<Body>>(private val initialNode: N) :
    NodeBuilder<Body, N> {

    override val node: Node<Body>
        get() = initialNode

    protected val agents = mutableListOf<AgentBuilder<*, *, Body>>()

    override fun <Belief : Any, Goal : Any> agent(block: AgentBuilder<Belief, Goal, Body>.() -> Unit) =
        buildAgent(null, block)

    override fun <Belief : Any, Goal : Any> agent(id: AgentID, block: AgentBuilder<Belief, Goal, Body>.() -> Unit) =
        buildAgent(id, block)

    override fun <Belief : Any, Goal : Any> withAgents(
        vararg agentFactories: (Node<Body>) -> AgentSpecification<Belief, Goal, Body>,
    ) = agentFactories.forEach { factory ->
        node.addAgent(factory)
    }

    private fun <Belief : Any, Goal : Any> buildAgent(
        id: AgentID?,
        block: AgentBuilder<Belief, Goal, Body>.() -> Unit,
    ) {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Body>(node, id)
        val agent: AgentBuilder<Belief, Goal, Body> = agentBuilder.apply(block)
        agents += agent
    }

    override fun build(): N {
        agents.forEach { factory -> node.addAgent({ factory.build() }) }
        return initialNode
    }
}
