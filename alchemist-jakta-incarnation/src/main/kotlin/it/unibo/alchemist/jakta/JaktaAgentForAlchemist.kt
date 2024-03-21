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
import it.unibo.tuprolog.solve.libs.oop.formalParameterTypes
import kotlin.reflect.KCallable
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.kotlinFunction

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
        AgentLifecycle.of(agent).reason(
            environment = jaktaEnvironment,
            controller = null,
        )
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
