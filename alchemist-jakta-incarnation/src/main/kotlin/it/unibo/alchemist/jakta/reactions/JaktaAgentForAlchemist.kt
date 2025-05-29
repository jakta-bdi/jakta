package it.unibo.alchemist.jakta.reactions

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.actions.effects.AddData
import it.unibo.jakta.actions.effects.BroadcastMessage
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.effects.PopMessage
import it.unibo.jakta.actions.effects.RemoveAgent
import it.unibo.jakta.actions.effects.RemoveData
import it.unibo.jakta.actions.effects.SendMessage
import it.unibo.jakta.actions.effects.SpawnAgent
import it.unibo.jakta.actions.effects.UpdateData
import it.unibo.jakta.alchemist.JaktaControllerForAlchemist
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.ACT
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.DELIBERATE
import it.unibo.jakta.alchemist.JaktaLifecyclePhase.SENSE
import it.unibo.tuprolog.solve.libs.oop.formalParameterTypes
import kotlin.reflect.KCallable
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.kotlinFunction

class JaktaAgentForAlchemist<P : Position<P>>(
    val jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
    timeDistribution: TimeDistribution<Any?>,
    val agent: ASAgent,
) : AbstractReaction<Any?>(jaktaEnvironment.node, timeDistribution) {
    private val agentLifecycle = AgentLifecycle.newLifecycleFor(agent)
    private val jaktaController = JaktaControllerForAlchemist(jaktaEnvironment)

    constructor(
        node: Node<Any?>,
        timeDistribution: TimeDistribution<Any?>,
        agentFactory: String,
        parameters: List<Any?>,
    ) : this(
        node.asProperty(),
        timeDistribution,
        agentFactory.reflectMethod(),
        parameters,
    )

    constructor(
        jaktaEnvironment: JaktaEnvironmentForAlchemist<P>,
        timeDistribution: TimeDistribution<Any?>,
        agentFactory: KCallable<ASAgent>,
        parameters: List<Any?>,
    ) : this(
        jaktaEnvironment,
        timeDistribution,
        with(agentFactory) {
            var parameterIndex = 0
            val supportedTypes =
                listOf(
                    jaktaEnvironment,
                    jaktaEnvironment.node,
                    jaktaEnvironment.randomGenerator,
                    jaktaEnvironment.alchemistEnvironment,
                )
            val actualParameters =
                formalParameterTypes.map { type ->
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

    override fun cloneOnNewNode(node: Node<Any?>, currentTime: it.unibo.alchemist.model.Time): Reaction<Any?> =
        JaktaAgentForAlchemist(
            node.asProperty<Any?, JaktaEnvironmentForAlchemist<P>>(),
            timeDistribution.cloneOnNewNode(node, currentTime),
            agent,
        )

    override fun execute() {
        val sideEffects: Iterable<EnvironmentChange> =
            when (val timeDist = timeDistribution) {
                is JaktaTimeDistribution ->
                    when (timeDist.phase) {
                        SENSE -> {
                            agentLifecycle.sense(
                                environment = jaktaEnvironment,
                                controller = jaktaController,
                                debugEnabled = false,
                            )
                            emptyList()
                        }
                        DELIBERATE -> {
                            agentLifecycle.deliberate()
                            emptyList()
                        }
                        ACT -> agentLifecycle.act(jaktaEnvironment)
                    }
                else ->
                    agentLifecycle.runOneCycle(
                        environment = jaktaEnvironment,
                        controller = jaktaController,
                        debugEnabled = false,
                    )
            }
        sideEffects.forEach {
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

    override fun updateInternalStatus(
        currentTime: it.unibo.alchemist.model.Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<Any?, *>?,
    ) { }

    override fun toString() = "${agent.name}@$timeDistribution"

    override fun canExecute() =
        jaktaEnvironment.alchemistEnvironment.simulation.time >= jaktaController.minimumAwakeTime

    companion object {
        fun String.reflectMethod(): KCallable<ASAgent> {
            val method = substringAfterLast('.')
            val className = substringBeforeLast('.')
            check(method.isNotEmpty() && className.isNotEmpty() && method != className) {
                "Invalid class $className or method $method"
            }
            val `class` = Class.forName(className)
            return `class`.methods
                .asSequence()
                .mapNotNull { it.kotlinFunction }
                .filter { it.returnType == ASAgent::class.starProjectedType }
                .filterIsInstance<KCallable<ASAgent>>()
                .first { it.name == method }
        }
    }
}
