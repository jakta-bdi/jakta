package it.unibo.jakta.dsl

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.ASAgent
import it.unibo.jakta.dsl.actions.ExternalActionsScope
import it.unibo.jakta.environment.Environment

class WrappedAgent(
    val agent: ASAgent,
    val actions: Map<String, ExternalAction>,
)

class JaktaForAlchemistMasScope : Builder<WrappedAgent> {
    lateinit var agents: ASAgent
    var actions: Map<String, ExternalAction> = emptyMap()

    fun environment(f: JaktaForAlchemistEnvironmentScope.() -> Unit): JaktaForAlchemistMasScope {
        actions += JaktaForAlchemistEnvironmentScope().also(f).build()
        return this
    }

    fun environment(e: Environment): JaktaForAlchemistMasScope {
        actions += e.externalActions
        return this
    }

    fun agent(name: String, f: AgentScope.() -> Unit): JaktaForAlchemistMasScope {
        agents = AgentScope(name).also(f).build()
        return this
    }

    override fun build(): WrappedAgent = WrappedAgent(agents, actions)
}

class JaktaForAlchemistEnvironmentScope : Builder<Map<String, ExternalAction>> {

    private val actionsScopes by lazy { ExternalActionsScope() }

    infix fun actions(actions: ExternalActionsScope.() -> Unit): JaktaForAlchemistEnvironmentScope {
        actionsScopes.also(actions)
        return this
    }

    override fun build(): Map<String, ExternalAction> = actionsScopes.build()
}

// TODO("This DSL entrypoint can create more than one agent, the simulation must know how to handle it.")
@JaktaDSL
fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.device(f: JaktaForAlchemistMasScope.() -> Unit): ASAgent {
    val wrappedAgent = JaktaForAlchemistMasScope().also(f).build()
    this.externalActions += wrappedAgent.actions
    return wrappedAgent.agent
}
