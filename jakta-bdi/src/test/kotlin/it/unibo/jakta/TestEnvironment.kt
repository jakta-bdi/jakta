package it.unibo.jakta

import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Var

fun main() {
    val env = BasicEnvironment()

    val start = Jakta.parseStruct("start(0, 10)")
    val alice =
        ASAgent.Companion.of(
            name = "alice",
            events = listOf(AchievementGoalInvocation(start)),
            planLibrary =
            mutableListOf(
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("start(N, N)"),
                    goals =
                    listOf(
                        Print(Atom.of("hello world"), Var.of("N")),
                    ),
                ),
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("start(N, M)"),
                    guard = Jakta.parseStruct("N < M & S is N + 1"),
                    goals =
                    listOf(
                        Print(Atom.of("hello world"), Var.of("N")),
                        Achieve(Jakta.parseStruct("start(S, M)")),
                    ),
                ),
            ),
        )

    val bob =
        ASAgent.Companion.of(
            name = "bob",
            events = listOf(AchievementGoalInvocation(start)),
            planLibrary =
            mutableListOf(
                ASNewPlan.ofAchievementGoalInvocation(
                    value = start,
                    goals =
                    listOf(
                        Print(Atom.of("Hello, my name is Bob")),
                    ),
                ),
            ),
        )

    val mas =
        Mas.of(
            ExecutionStrategy
                .oneThreadPerAgent(),
            env,
            alice,
            bob,
        )

    mas.start()
}
