package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.actions.JaktaStepAction
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.conditions.AbstractCondition
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.jakta.node.RuntimeNodes
import kotlin.reflect.KCallable
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.kotlinFunction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Jakta incarnation for executing on Alchemist.
 */
class JaktaIncarnation<P : Position<P>> : Incarnation<Any?, P> {
    override fun getProperty(node: Node<Any?>, molecule: Molecule, property: String): Double =
        when (val concentration = node.getConcentration(molecule)) {
            is Number -> concentration.toDouble()
            is String -> concentration.toDoubleOrNull()
            is Boolean -> if (concentration) 1.0 else 0.0
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
        it.addProperty(JaktaForAlchemistRuntime(environment, it, randomGenerator))
    }

    /**
     * Scalar `time-distribution` values are the rate of a [DiracComb].
     * Other distributions can be declared with their type, e.g. `{ type: ExponentialTime, parameters: [1] }`,
     * and are then built by Alchemist without calling this method.
     */
    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        parameter: Any?,
    ): TimeDistribution<Any?> = DiracComb(
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
        additionalParameters: Any?,
    ): Action<Any?> {
        // requireNotNull(node) { "JaKtA cannot execute as a Global Reaction" }
        error { "Alchemist actions direct creation is not supported by Jakta" }
    }

    /**
     * Creates the reaction running the JaKtA nodes of the [node].
     * The [parameter] is either the entrypoint, as a classpath string `<JVM class>.<function>`,
     * or a map with the `entrypoint` and, optionally, the [Messaging] mode as `messaging`
     * (`global`, the default, or `neighborhood`).
     */
    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        timeDistribution: TimeDistribution<Any?>,
        parameter: Any?,
    ): Reaction<Any?> {
        requireNotNull(node) { "JaKtA cannot execute as a Global Reaction" }
        val runtime = node.asPropertyOrNull<Any?, JaktaForAlchemistRuntime<P>>()
        checkNotNull(runtime) {
            "The JaKtA incarnation expects a JaktaForAlchemistRuntime property associated to the Alchemist Node."
        }
        val entrypoint = when (parameter) {
            is Map<*, *> -> {
                parameter[MESSAGING]?.let { runtime.messaging = Messaging.parse(it) }
                parameter[ENTRYPOINT]
            }

            else -> parameter
        }
        runtime.setInitialJaktaNodes(loadEntrypointFromClasspath(entrypoint, node))
        return Event(node, timeDistribution).also { it.actions = listOf(JaktaStepAction(runtime)) }
    }

    override fun createCondition(
        randomGenerator: RandomGenerator?,
        environment: Environment<Any?, P>?,
        node: Node<Any?>?,
        actionable: Actionable<Any?>?,
        additionalParameters: Any?,
    ): Condition<Any?> = object : AbstractCondition<Any>(requireNotNull(node)) {
        override fun getContext() = Context.LOCAL
        override fun getPropensityContribution(): Double = 1.0
        override fun isValid(): Boolean = true
    }

    /**
     * Utilities for Jakta incarnation.
     */
    companion object {
        private const val ENTRYPOINT = "entrypoint"
        private const val MESSAGING = "messaging"

        /**
         * Function that loads the value passed as entrypoint in the simulation from the classpath.
         * @param entrypoint the parameter specified in the simulation configuraiton.
         * @param node the alchemist Node on which the jakta entrypoint should be executed.
         * @return an instance of [RuntimeNodes] which contains the jakta nodes to be executed on the alchemist node.
         */
        fun loadEntrypointFromClasspath(entrypoint: Any?, node: Node<Any?>): RuntimeNodes<*> {
            require(entrypoint is String) {
                "JaKtA expects the program to be the entrypoint as a classpath string (<JVM class>.<function>), " +
                    "or a map with an '$ENTRYPOINT' key, but got: $entrypoint"
            }

            // Load entrypoint from classpath with reflection
            val method = entrypoint.substringAfterLast('.')
            val className = entrypoint.substringBeforeLast('.')
            check(method.isNotEmpty() && className.isNotEmpty() && method != className) {
                "Invalid class $className or method $method"
            }

            val `class` = Class.forName(className)
            val callableFunction = `class`.methods
                .asSequence()
                .mapNotNull { it.kotlinFunction }
                .filter { it.returnType.isSubtypeOf(RuntimeNodes::class.starProjectedType) }
                .filterIsInstance<KCallable<RuntimeNodes<*>>>()
                .first { it.name == method }

            val jaktaRuntime = node.asPropertyOrNull<Any?, JaktaForAlchemistRuntime<*>>()
            checkNotNull(jaktaRuntime) {
                "The node does not have a JaKtA Runtime property, cannot create an Alchemist Action."
            }
            return callableFunction.call(jaktaRuntime)
        }
    }
}
