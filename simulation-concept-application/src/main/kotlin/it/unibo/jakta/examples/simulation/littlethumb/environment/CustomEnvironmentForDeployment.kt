package it.unibo.jakta.examples.simulation.littlethumb.environment

import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.environment.Environment

open class ActionsImplementation : LittlethumbActions {

    override fun move(): ExternalAction = object : AbstractExternalAction("move", 0) {
        override fun action(request: ExternalRequest) {
            println("move in deployment implementation")
        }
    }

    override fun goTo(): ExternalAction = object : AbstractExternalAction("goTo", 1) {
        override fun action(request: ExternalRequest) {
            println("goTo in deployment implementation")
        }
    }

    override fun stopMessage(): ExternalAction = object : AbstractExternalAction("stopAgent", 1) {
        override fun action(request: ExternalRequest) {
            println("stopMessage in deployment implementation")
        }
    }

    override fun put(): ExternalAction = object : AbstractExternalAction("put", 0) {
        override fun action(request: ExternalRequest) {
            println("put in deployment implementation")
        }
    }

    override fun greet(): ExternalAction = object : AbstractExternalAction("greet", 0) {
        override fun action(request: ExternalRequest) {
            println("Hi, my name is ${request.sender} and I'm running on node ${request.environment}")
        }
    }
}

class CustomEnvironmentForDeployment(
    val customisation: ActionsImplementation = ActionsImplementation(),
    val environment: Environment = Environment.of(externalActions = customisation.generateExternalActions()),
) : Environment by environment
