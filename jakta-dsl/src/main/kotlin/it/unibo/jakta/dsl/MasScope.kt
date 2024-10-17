package it.unibo.jakta.dsl

import it.unibo.jakta.Agent
import it.unibo.jakta.Mas
import it.unibo.jakta.dsl.environment.EnvironmentScope
import it.unibo.jakta.environment.Environment

@JaktaDSL
class MasScope : Builder<Mas> {
    var env: Environment = Environment.of()
    var agents: List<Agent> = emptyList()
    var executionStrategy = it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerMas()

    fun environment(f: EnvironmentScope.() -> Unit): MasScope {
        env = EnvironmentScope().also(f).build()
        return this
    }

    fun environment(environment: Environment): MasScope {
        env = environment
        return this
    }

    fun agent(name: String, f: AgentScope.() -> Unit): MasScope {
        agents += AgentScope(name).also(f).build()
        return this
    }

    fun executionStrategy(f: () -> it.unibo.jakta.executionstrategies.ExecutionStrategy): MasScope {
        executionStrategy = f()
        return this
    }

    override fun build(): Mas = Mas.of(executionStrategy, env, agents)
}
