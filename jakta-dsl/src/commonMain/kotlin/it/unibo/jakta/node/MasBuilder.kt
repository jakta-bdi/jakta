package it.unibo.jakta.node

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.environment.Runtime
import it.unibo.jakta.node.baseImpl.MASImpl

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Runtime> {

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    @JaktaDSL
    fun <Skills : Any> agent(block: AgentBuilder<Belief, Goal, Skills, Env>.() -> Unit)

    /**
     * Defines an agent with a specific name using the provided builder block.
     * @return the constructed agent.
     */
    fun <Skills : Any> agent(name: String, block: AgentBuilder<Belief, Goal, Skills, Env>.() -> Unit)

//    /**
//     * Adds multiple agents to the MAS.
//     */
//    fun withAgents(vararg agents: Agent<Belief, Goal>)

    /**
     * Defines the environment instance for the MAS.
     * @param[block] a function that constructs the environment.
     */
    fun environment(block: () -> Env)

    /**
     * Builds and returns the MAS instance.
     */
    fun build(): Node<Belief, Goal, Env>
}

/**
 * Implementation of the MasBuilder interface.
 */
open class MasBuilderImpl<Belief : Any, Goal : Any, Env : Runtime> : MasBuilder<Belief, Goal, Env> {
    protected var environment: Env? = null
    protected val agents = mutableListOf<AgentBuilder<Belief, Goal, *, Env>>()

    override fun <Skills : Any> agent(block: AgentBuilder<Belief, Goal, Skills, Env>.() -> Unit) =
        buildAgent(null, block)

    override fun <Skills : Any> agent(
        name: String,
        block: AgentBuilder<Belief, Goal, Skills, Env>.() -> Unit
    ) = buildAgent(name, block)

    private fun <Skills : Any> buildAgent(
        name: String?,
        block: AgentBuilder<Belief, Goal, Skills, Env>.() -> Unit,
    ) {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Skills, Env>( name)
        val agent = agentBuilder.apply(block)
        agents += agent
    }

//    override fun withAgents(vararg agents: Agent<Belief, Goal>) {
//        this.agents += agents
//    }

    override fun environment(block: () -> Env) {
        environment = block()
    }

    override fun build(): Node<Belief, Goal, Env> {
        val env = environment ?: error { "Must provide an Environment for the MAS" }
        agents.forEach {env.addAgent(it.build(env))} //TODO here like this? or change the DSL?
        return MASImpl(env)
    }
}
