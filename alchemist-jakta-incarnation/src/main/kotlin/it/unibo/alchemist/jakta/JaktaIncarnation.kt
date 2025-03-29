package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist.Companion.BROKER_MOLECULE
import it.unibo.alchemist.jakta.reactions.JaktaAgentForAlchemist
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
import it.unibo.alchemist.model.timedistributions.DiracComb
import org.apache.commons.math3.random.RandomGenerator

class JaktaIncarnation<P> : Incarnation<Any?, P> where P : Position<P> {
    override fun getProperty(
        node: Node<Any?>,
        molecule: Molecule,
        property: String,
    ): Double =
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
    ): Action<Any?> = error("No actions in Jakta")

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
        additionalParameters: Any?,
    ): Reaction<Any?> {
        /*
         * additionalParameters will contain everything that was passed as `program`
         *
         *       agent-factory: it.unibo.jakta.test.SharedToken.entrypoint
         *       parameters: []
         */
        val (entrypoint: String, parameters: List<Any?>) =
            when (additionalParameters) {
                is Map<*, *> ->
                    additionalParameters[FACTORY_KEY].toString() to (additionalParameters[PARAMETERS_KEY] as List<Any?>)
                is String -> additionalParameters to emptyList()
                else ->
                    error(
                        """
                |Invalid JaKtA parameters $additionalParameters, expected either:
                |- a String with the agent factory method name, or
                |- a Map with the following keys:
                |   - $FACTORY_KEY: the agent factory method name
                |   - $PARAMETERS_KEY: the list of parameters to pass to the agent factory method
                        """.trimMargin(),
                    )
            }
        return JaktaAgentForAlchemist(
            requireNotNull(node) { "Jakta can not execute as global reaction" },
            timeDistribution,
            entrypoint,
            parameters,
        )
    }

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator?,
        environment: Environment<Any?, P>?,
        node: Node<Any?>?,
        parameter: Any?,
    ): TimeDistribution<Any?> =
        DiracComb(
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
    ): Node<Any?> =
        GenericNode(this, environment).also {
            it.addProperty(JaktaEnvironmentForAlchemist(environment, randomGenerator, it))
            it.setConcentration(BROKER_MOLECULE, JaktaForAlchemistMessageBroker(environment))
        }

    companion object {
        const val FACTORY_KEY = "agent-factory"
        const val PARAMETERS_KEY = "parameters"
    }
}
