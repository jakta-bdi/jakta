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
import it.unibo.jakta.agents.bdi.actions.effects.AddData
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.actions.effects.RemoveAgent
import it.unibo.jakta.agents.bdi.actions.effects.RemoveData
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.actions.effects.SpawnAgent
import it.unibo.jakta.agents.bdi.actions.effects.UpdateData
import it.unibo.tuprolog.solve.libs.oop.formalParameterTypes
import kotlin.reflect.KCallable
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.kotlinFunction

class JaktaAgentForAlchemist<P : Position<P>>(
    val jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
    val agent: Agent,
) : AbstractAction<Any?>(jaktaEnvironment.node) {

    private val agentLifecycle = AgentLifecycle.of(agent)
    constructor(
        node: Node<Any?>,
        agentFactory: String,
        parameters: List<Any?>,
    ) : this(
        node.asProperty<Any?, JaktaEnvironmentForAlchemist<P>>(),
        agentFactory.reflectMethod(),
        parameters,
    )

    constructor(
        jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
        agentFactory: KCallable<Agent>,
        parameters: List<Any?>,
    ) : this(
        jaktaEnvironment,
        with(agentFactory) {
            var parameterIndex = 0
            val supportedTypes = listOf(
                jaktaEnvironment,
                jaktaEnvironment.node,
                jaktaEnvironment.randomGenerator,
                jaktaEnvironment.alchemistEnvironment,
            )
            val actualParameters = formalParameterTypes.map { type ->
                val implicitParameter = supportedTypes.firstOrNull { type.isInstance(it) }
                if (implicitParameter == null) {
                    check(parameterIndex < parameters.size) {
                        "Invalid number of parameters for $agentFactory: " +
                            "expected at least $parameterIndex parameters, got ${parameters.size}"
                    }
                    parameters[parameterIndex++]
                } else {
                    implicitParameter
                }
            }
            call(*actualParameters.toTypedArray())
        },
    )

    init {
        declareDependencyTo(Dependency.EVERYTHING)
    }

    override fun cloneAction(node: Node<Any?>?, reaction: Reaction<Any?>?): Action<Any?> {
        TODO("Not yet implemented")
    }

    override fun execute() {
        val environmentSideEffects = agentLifecycle.reason(
            environment = jaktaEnvironment,
            controller = null,
            debugEnabled = false,
        )

        environmentSideEffects.forEach {
            when (it) {
                is BroadcastMessage -> jaktaEnvironment.broadcastMessage(it.message)
                is RemoveAgent -> jaktaEnvironment.removeAgent(it.agentName)
                is SendMessage -> jaktaEnvironment.submitMessage(it.recipient, it.message)
                is SpawnAgent -> jaktaEnvironment.addAgent(it.agent)
                is AddData -> jaktaEnvironment.addData(it.key, it.value)
                is RemoveData -> jaktaEnvironment.removeData(it.key)
                is UpdateData -> jaktaEnvironment.updateData(it.newData)
                is PopMessage -> jaktaEnvironment.popMessage(it.agentName)
            }
        }
    }

    override fun getContext() = Context.GLOBAL

    companion object {
        fun String.reflectMethod(): KCallable<Agent> {
            val method = substringAfterLast('.')
            val className = substringBeforeLast('.')
            check(method.isNotEmpty() && className.isNotEmpty() && method != className) {
                "Invalid class $className or method $method"
            }
            val `class` = Class.forName(className)
            return `class`.methods
                .asSequence()
                .mapNotNull { it.kotlinFunction }
                .filter { it.returnType == Agent::class.starProjectedType }
                .filterIsInstance<KCallable<Agent>>()
                .first { it.name == method }
        }
    }
}
