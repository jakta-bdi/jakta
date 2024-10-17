package it.unibo.jakta

import it.unibo.jakta.environment.Environment
import it.unibo.jakta.executionstrategies.ExecutionStrategy

fun main() {
    val e = Environment.of()

    val alice = Agent.of()
    val bob = Agent.of()

    val agentSystem = Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
