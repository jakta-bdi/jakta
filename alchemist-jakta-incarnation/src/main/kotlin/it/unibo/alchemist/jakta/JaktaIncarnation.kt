package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.actions.JaktaForAlchemistAgent
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.jakta.dsl.RuntimeNodes
import it.unibo.jakta.node.Node as JaktaNode
import kotlin.collections.get
import kotlin.reflect.KCallable
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import org.apache.commons.math3.random.RandomGenerator
import kotlin.reflect.jvm.kotlinFunction

class JaktaIncarnation<P: Position<P>> : Incarnation<Any?, P> {
    override fun getProperty(
        node: Node<Any?>,
        molecule: Molecule,
        property: String
    ): Double = when (val concentration = node.getConcentration(molecule)) {
        is Number -> concentration.toDouble()
        is String -> concentration.toDoubleOrNull()
        else -> null
    } ?: Double.NaN

    override fun createMolecule(s: String): Molecule = SimpleMolecule(s)

    override fun createConcentration(descriptor: Any?): Any? = descriptor

    override fun createConcentration(): Any? = null

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        parameter: Any?,
    ): Node<Any?> = GenericNode(environment).also {
        it.addProperty(JaktaForAlchemistRuntime(environment, it))
        // TODO("Is there a way to inject nodes in Jakta Runtime?")
    }

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        parameter: Any?,
    ): TimeDistribution<Any?> = DiracComb( //TODO("What if I create a different type of reaction from yaml?")
        when (parameter) {
            is Number -> parameter.toDouble()
            is String -> parameter.toDouble()
            else -> 1.0
        },
    )

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        actionable: Actionable<Any?>,
        additionalParameters: Any?
    ): Action<Any?> {
        //requireNotNull(node) { "JaKtA cannot execute as a Global Reaction" }
        error { "Alchemist actions direct creation is not supported by Jakta" }
    }

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        timeDistribution: TimeDistribution<Any?>,
        parameter: Any?
    ): Reaction<Any?> {
        requireNotNull(node) { "JaKtA cannot execute as a Global Reaction" }
        val jaktaNodes = loadEntrypointFromClasspath(parameter, node)
        val runtime = node.properties.filterIsInstance<JaktaForAlchemistRuntime<P>>().firstOrNull()
        checkNotNull(runtime) {
            "The JaKtA incarnation expects a JaktaForAlchemistRuntime property associated to the Alchemist Node."
        }
        runtime.setInitialJaktaNodes(jaktaNodes)

        val event = Event(node, timeDistribution)
        event.actions = runtime.getAgentActions().map { it.second }
        return event
    }

    override fun createCondition(
        randomGenerator: RandomGenerator?,
        environment: Environment<Any?, P>?,
        node: Node<Any?>?,
        actionable: Actionable<Any?>?,
        additionalParameters: Any?
    ): Condition<Any?> = object : AbstractCondition<Any>(requireNotNull(node)) {
        override fun getContext() = Context.LOCAL
        override fun getPropensityContribution(): Double = 1.0
        override fun isValid(): Boolean = true
    }

    companion object {
        fun loadEntrypointFromClasspath(entrypoint: Any?, node: Node<Any?>): RuntimeNodes<JaktaNode<*,*>> {
            require(entrypoint is String) {
                "JaKtA expects the program to be the classpath String pointing to program entrypoint."
            }

            // Load entrypoint from classpath with reflection
            val method = entrypoint.substringAfterLast('.')
            val className = entrypoint.substringBeforeLast('.')
            check(method.isNotEmpty() && className.isNotEmpty() && method != className) {
                "Invalid class $className or method $method"
            }

            val `class` = Class.forName(className)
            println(`class`.methods)
            val callableFunction = `class`.methods
                .asSequence()
                .mapNotNull { it.kotlinFunction }
                .filter { it.returnType.isSubtypeOf(RuntimeNodes::class.starProjectedType) }
                .filterIsInstance<KCallable<RuntimeNodes<JaktaNode<*,*>>>>()
                .first { it.name == method }

            val jaktaRuntime = node.properties
                .filterIsInstance<JaktaForAlchemistRuntime<*>>()
                .firstOrNull()
            checkNotNull(jaktaRuntime) {
                "The node does not have a JaKtA Runtime property, cannot create an Alchemist Action."
            }
            // Add nodes here inside of jakta runtime?
            val runtimeNodes = callableFunction.call(jaktaRuntime)
            return runtimeNodes
        }
    }
}
