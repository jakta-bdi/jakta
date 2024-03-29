package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy

fun main() {
    val e = Environment.of()

    val alice = Agent.of()
    val bob = Agent.of()

    val agentSystem = Mas.of(ExecutionStrategy.oneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
