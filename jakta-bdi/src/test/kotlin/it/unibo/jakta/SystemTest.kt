package it.unibo.jakta

import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.executionstrategies.ExecutionStrategy

fun main() {
    val e = BasicEnvironment()

    val alice = ASAgent.of()
    val bob = ASAgent.of()

    val agentSystem =
        Mas.of(
            ExecutionStrategy
                .oneThreadPerAgent(),
            e,
            alice,
            bob,
        )

    agentSystem.start()
}
