package it.unibo.alchemist.jakta

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import org.apache.commons.math3.random.RandomGenerator

class JaktaIncarnation<P> : Incarnation<Any?, P> where P : Position<P> {
    override fun getProperty(node: Node<Any?>, molecule: Molecule, property: String): Double =
        when (val concentration = node.getConcentration(molecule)) {
            is Number -> concentration.toDouble()
            is String -> concentration.toDoubleOrNull()
            else -> null
        } ?: Double.NaN

    override fun createMolecule(s: String): Molecule = SimpleMolecule(s)

    override fun createConcentration(s: Any?): Any? = s

    override fun createConcentration(): Any? = null

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        time: TimeDistribution<Any?>,
        actionable: Actionable<Any?>,
        additionalParameters: Any?,
    ): Action<Any?> {
        /*
         * additionalParameters will contain everything that was passed as `program`
         *
         *       agent-factory: it.unibo.jakta.test.SharedToken.entrypoint
         *       parameters: []
         */
        val (entrypoint: String, parameters: List<Any?>) = when (additionalParameters) {
            is Map<*, *> ->
                additionalParameters[factoryKey].toString() to (additionalParameters[parametersKey] as List<Any?>)
            is String -> additionalParameters to emptyList()
            else -> error(
                """
                |Invalid JaKtA parameters $additionalParameters, expected either:
                |- a String with the agent factory method name, or
                |- a Map with the following keys:
                |   - $factoryKey: the agent factory method name
                |   - $parametersKey: the list of parameters to pass to the agent factory method
                """.trimMargin(),
            )
        }
        return JaktaAgentForAlchemist(
            requireNotNull(node) { "Jakta can not execute as global reaction" },
            entrypoint,
            parameters,
        )
    }

    override fun createCondition(
        randomGenerator: RandomGenerator?,
        environment: Environment<Any?, P>?,
        node: Node<Any?>?,
        time: TimeDistribution<Any?>?,
        actionable: Actionable<Any?>?,
        additionalParameters: Any?,
    ): Condition<Any?> = error("No conditions in Jakta")

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>?,
        timeDistribution: TimeDistribution<Any?>,
        parameter: Any?,
    ): Reaction<Any?> = Event(node, timeDistribution).also {
        it.actions = listOf(createAction(randomGenerator, environment, node, timeDistribution, it, parameter))
    }

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator?,
        environment: Environment<Any?, P>?,
        node: Node<Any?>?,
        parameter: Any?,
    ): TimeDistribution<Any?> = DiracComb(
        when (parameter) {
            is Number -> parameter.toDouble()
            is String -> parameter.toDouble()
            else -> error("Invalid frequency: $parameter")
        },
    )

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        parameter: Any?,
    ): Node<Any?> = GenericNode(this, environment).also {
        it.addProperty(JaktaEnvironmentForAlchemist(environment, randomGenerator, it))
    }

    companion object {
        const val factoryKey = "agent-factory"
        const val parametersKey = "parameters"
    }
}
