package it.unibo.jakta

import it.unibo.jakta.environment.Environment

fun main() {
    val e = Environment.of()

    val alice = ASAgent.of()
    val bob = ASAgent.of()

    val agentSystem = Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
