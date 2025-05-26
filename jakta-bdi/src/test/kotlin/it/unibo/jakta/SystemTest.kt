package it.unibo.jakta

import it.unibo.jakta.environment.BasicEnvironment

fun main() {
    val e = BasicEnvironment()

    val alice = ASAgent.of()
    val bob = ASAgent.of()

    val agentSystem = Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), e, alice, bob)

    agentSystem.start()
}
