package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.examples.simulation.littlethumb.environment.CustomEnvironmentForDeployment

fun main() {
    mas {
        environment(CustomEnvironmentForDeployment())
        agents = listOf(pollicino(), pollicina())
    }.start()
}
