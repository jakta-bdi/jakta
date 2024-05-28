package it.unibo.alchemist.jakta

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.valueOrEmptyMolecule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.reflect.KProperty

class JaktaForAlchemistLibrary<P : Position<P>>(private val env: JaktaEnvironmentForAlchemist<P>) {

    /**
     * Delegate to help implementing Jakta External Actions.
     */
    class ExternalActionFor(
        val arity: Int,
        val body: ExternalRequest.() -> Unit,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ExternalAction {
            return object : AbstractExternalAction(property.name, arity) {
                override fun action(request: ExternalRequest) {
                    request.body()
                }
            }
        }
    }

    /**
     * External action that writes information into the Alchemist environment.
     */
    val writeData = object : AbstractExternalAction("writeData", 2) {
        override fun action(request: ExternalRequest) {
            val name = request.arguments[0].castToAtom().value
            val concentration = request.arguments[1].`as`<ObjectRef>()?.`object`.valueOrEmptyMolecule()
            env.node.setConcentration(SimpleMolecule(name), concentration)
        }
    }

    val brownianMove = object : AbstractExternalAction("brownianMove", 0) {
        override fun action(request: ExternalRequest) {
            val deltaX = (env.randomGenerator.nextFloat() - 0.5) * 0.05
            val deltaY = (env.randomGenerator.nextFloat() - 0.5) * 0.05
            val position = env.alchemistEnvironment.getPosition(env.node)
            val newPosition = env.alchemistEnvironment.makePosition(
                position.coordinates[0] + deltaX,
                position.coordinates[1] + deltaY,
            )
            println("Moving node ${env.node.id} into position ($newPosition)")
            env.alchemistEnvironment.moveNodeToPosition(
                env.node,
                newPosition,
            )
        }
    }

    /**
     * Generates the [Pair] containing the external action name and the [ExternalAction] object itself.
     */
    val ExternalAction.named: Pair<String, ExternalAction> get() = signature.name to this

    fun api() = mapOf(
        writeData.named,
        brownianMove.named,
    )
}
