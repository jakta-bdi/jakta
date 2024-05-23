package it.unibo.jakta.examples.simulation.littlethumb.environment

import it.unibo.jakta.agents.bdi.actions.ExternalAction

interface LittlethumbActions {
    fun move(): ExternalAction
    fun goTo(): ExternalAction
    fun stopMessage(): ExternalAction
    fun put(): ExternalAction
    fun greet(): ExternalAction

    fun generateExternalActions(): Map<String, ExternalAction> = mapOf(
        goTo().let { it.signature.name to it },
        stopMessage().let { it.signature.name to it },
        move().let { it.signature.name to it },
        put().let { it.signature.name to it },
        greet().let { it.signature.name to it },
    )
}
