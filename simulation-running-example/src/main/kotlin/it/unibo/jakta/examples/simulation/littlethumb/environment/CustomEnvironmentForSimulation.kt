package it.unibo.jakta.examples.simulation.littlethumb.environment

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.utils.fix
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class CustomEnvironmentForSimulation<P : Position<P>>(
    val jaktaForAlchemistEnvironment: JaktaEnvironmentForAlchemist<P>,
) : LittlethumbActions, Environment by jaktaForAlchemistEnvironment {

    override fun move(): ExternalAction = object : AbstractExternalAction("move", 0) {
        override fun action(request: ExternalRequest) {
            jaktaForAlchemistEnvironment.alchemistEnvironment.moveNodeToPosition(
                jaktaForAlchemistEnvironment.node,
                movementInGrid(jaktaForAlchemistEnvironment, pollicinoDirections),
            )
        }
    }

    override fun goTo(): ExternalAction = object : AbstractExternalAction("goTo", 1) {
        override fun action(request: ExternalRequest) {
            val arg = request.arguments.first().fix<Pair<Int, Position<*>>>()
            val nodeId = arg.first
            val position = arg.second
            if (jaktaForAlchemistEnvironment.alchemistEnvironment.getNodeByID(nodeId).contents.isNotEmpty()) {
                jaktaForAlchemistEnvironment.alchemistEnvironment
                    .getNodeByID(nodeId)
                    .removeConcentration(SimpleMolecule("breadCrumb"))
                jaktaForAlchemistEnvironment.alchemistEnvironment.moveNodeToPosition(
                    jaktaForAlchemistEnvironment.node,
                    jaktaForAlchemistEnvironment.alchemistEnvironment.makePosition(
                        position.getCoordinate(0),
                        position.getCoordinate(1),
                    ),
                )
            }
        }
    }

    override fun stopMessage(): ExternalAction = object : AbstractExternalAction("stopAgent", 1) {
        override fun action(request: ExternalRequest) {
            val name = request.arguments.first().fix<String>()
            val payload: Struct = Atom.of("stop")
            sendMessage("$name@1", Message(request.sender, Tell, payload))
        }
    }

    override fun put(): ExternalAction = object : AbstractExternalAction("put", 0) {
        override fun action(request: ExternalRequest) {
            val position = jaktaForAlchemistEnvironment.alchemistEnvironment
                .getPosition(jaktaForAlchemistEnvironment.node)
            if (jaktaForAlchemistEnvironment.randomGenerator.nextDouble() < 0.15) {
                jaktaForAlchemistEnvironment.alchemistEnvironment.addNode(
                    GenericNode(jaktaForAlchemistEnvironment.alchemistEnvironment).also {
                        it.setConcentration(SimpleMolecule("breadCrumb"), it.id to position)
                    },
                    position,
                )
            }
        }
    }

    override fun greet(): ExternalAction = object : AbstractExternalAction("greet", 0) {
        override fun action(request: ExternalRequest) {
            println(
                "Hi, my name is ${request.sender}," +
                    " I'm running on node ${jaktaForAlchemistEnvironment.node.id}," +
                    " Time is ${request.requestTimestamp}",
            )
        }
    }

    override val externalActions: Map<String, ExternalAction> = jaktaForAlchemistEnvironment.externalActions +
        generateExternalActions()
}