package it.unibo.alchemist.jakta

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import kotlin.reflect.KCallable

class JaktaAgentForAlchemist<P : Position<P>>(
    val jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
    val agent: Agent,
) : AbstractAction<Any?>(jaktaEnvironment.node) {

    constructor(
        node: Node<Any?>,
        agentFactory: String,
        parameters: List<Any?>,
    ) : this(
        node.asProperty<Any?, JaktaEnvironmentForAlchemist<P>>(),
        agentFactory.reflectMethod().call(*parameters.toTypedArray()),
    )

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

    companion object {
        fun String.reflectMethod(): KCallable<Agent> {
            val method = substringAfterLast('.')
            val `class` = substringBeforeLast('.')
            return Class.forName(`class`).kotlin.members
                .filterIsInstance<KCallable<Agent>>()
                .first { it.name == method }
        }
    }
}
