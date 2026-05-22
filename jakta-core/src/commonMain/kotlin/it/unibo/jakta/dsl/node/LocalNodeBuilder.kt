package it.unibo.jakta.dsl.node

import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.agent.AgentBuilderImpl
import it.unibo.jakta.node.LocalNode

/**
 * Implementation of the MasBuilder interface.
 */
open class LocalNodeBuilder<Skills : Any, Body : Any> : NodeBuilder<Skills, Body, LocalNode<Body, Skills>> {

    private val node = LocalNode<Body, Skills>()

    protected val agents = mutableListOf<AgentBuilder<*, *, Skills, Body>>()

    override fun <Belief : Any, Goal : Any> agent(block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit) =
        buildAgent(null, block)

    override fun <Belief : Any, Goal : Any> agent(
        name: String,
        block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit,
    ) = buildAgent(name, block)

    private fun <Belief : Any, Goal : Any> buildAgent(
        name: String?,
        block: AgentBuilder<Belief, Goal, Skills, Body>.() -> Unit,
    ) {
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
        agents.forEach { node.addAgent(it.build(node)) }
        return node
    }
}
