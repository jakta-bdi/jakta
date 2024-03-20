package it.unibo.alchemist.jakta

import it.unibo.alchemist.model.*
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import org.danilopianini.util.ListSet

class JaktaAgentForAlchemist<P: Position<P>>(
    val jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
    val agent: Agent
) : AbstractAction<Any?>(jaktaEnvironment.node){

    init {
        declareDependencyTo(Dependency.EVERYTHING)
    }

    override fun cloneAction(node: Node<Any?>?, reaction: Reaction<Any?>?): Action<Any?> {
        TODO("Not yet implemented")
    }

    override fun execute() {
        AgentLifecycle.of(agent).reason(
            environment = jaktaEnvironment,
            controller = null,
        )
    }

    override fun getContext() = Context.GLOBAL
}