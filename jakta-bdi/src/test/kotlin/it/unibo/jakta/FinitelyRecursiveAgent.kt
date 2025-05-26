package it.unibo.jakta

import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.plans.ASPlan

fun main() {
    val start = Jakta.parseStruct("start(0, 120)")

    val alice = ASAgent.of(
        name = "alice",
        events = listOf(AchievementGoalInvocation(start)),
        planLibrary = mutableListOf(
            ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, S)"),
                goals = listOf(
                    Print(Jakta.parseStruct("print(\"hello world\", S)")),
                ),
            ),
            ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, M)"),
                guard = Jakta.parseStruct("S < M & N is S + 1"),
                goals = listOf(
                    Print(Jakta.parseStruct("print(\"hello world\", S)")),
                    Achieve(Jakta.parseStruct("start(N, M)")),
                ),
            ),
        ),
    )

    val mas = Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        BasicEnvironment(),
        alice,
    )
    // println(Jakta.printAslSyntax(alice))
    mas.start()
}
