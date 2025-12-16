package it.unibo.jakta.mas

import it.unibo.jakta.JaktaDSL
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentBuilder
import it.unibo.jakta.agent.AgentBuilderImpl
import it.unibo.jakta.environment.Environment

/**
 * Builder interface for defining a Multi-Agent System (MAS) with agents and an environment.
 */
@JaktaDSL
interface MasBuilder<Belief : Any, Goal : Any, Env : Environment<Belief, Goal>> {

    /**
     * Defines an agent using the provided builder block.
     * @return the constructed agent.
     */
    @JaktaDSL
    fun agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal>

    /**
     * Defines an agent with a specific name using the provided builder block.
     * @return the constructed agent.
     */
    fun agent(name: String, block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal>

    /**
     * Adds multiple agents to the MAS.
     */
    fun withAgents(vararg agents: Agent<Belief, Goal>)

    /**
     * Defines the environment instance for the MAS.
     * @param[block] a function that constructs the environment.
     */
    fun environment(block: () -> Env)

    /**
     * Builds and returns the MAS instance.
     */
    fun build(): MAS<Belief, Goal, Env>
}

/**
 * Implementation of the MasBuilder interface.
 */
open class MasBuilderImpl<Belief : Any, Goal : Any, Env : Environment<Belief, Goal>> : MasBuilder<Belief, Goal, Env> {
    protected var environment: Env? = null
    protected val agents = mutableListOf<Agent<Belief, Goal>>()

    override fun agent(block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal> =
        buildAgent(null, block)

    override fun agent(name: String, block: AgentBuilder<Belief, Goal, Env>.() -> Unit): Agent<Belief, Goal> =
        buildAgent(name, block)

    private fun buildAgent(
        name: String?,
        block: AgentBuilder<Belief, Goal, Env>.() -> Unit,
    ): Agent<Belief, Goal> {
        val agentBuilder = AgentBuilderImpl<Belief, Goal, Env>(name)
        val agent = agentBuilder.apply(block).build()
        agents += agent
        return agent
    }

    override fun withAgents(vararg agents: Agent<Belief, Goal>) {
        this.agents += agents
    }

    override fun environment(block: () -> Env) {
        environment = block()
    }

    override fun build(): MAS<Belief, Goal, Env> {
        val env = environment ?: error { "Must provide an Environment for the MAS" }
        agents.forEach {env.addAgent(it)} //TODO here like this? or change the DSL?
        return MASImpl(env)
    }
}
