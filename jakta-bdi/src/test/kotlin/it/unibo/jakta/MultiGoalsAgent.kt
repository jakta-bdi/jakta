package it.unibo.jakta

import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy

fun main() {
    val agent =
        ASAgent.of(
            name = "agent",
            events =
            listOf(
                AchievementGoalInvocation(Jakta.parseStruct("count(0, 10, up)")),
                AchievementGoalInvocation(Jakta.parseStruct("count(100, 90, down)")),
            ),
            planLibrary =
            mutableListOf(
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("count(N, N, Dir)"),
                    goals = listOf(Print(Jakta.parseStruct("print(\"End of\", Dir)"))),
                ),
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("count(N, M, up)"),
                    guard = Jakta.parseStruct("N < M & S is N + 1"),
                    goals =
                    listOf(
                        Print(Jakta.parseStruct("print(\"Up\", N)")),
                        Achieve(Jakta.parseStruct("count(S, M, up)")),
                    ),
                ),
                ASNewPlan.ofAchievementGoalInvocation(
                    value = Jakta.parseStruct("count(N, M, down)"),
                    guard = Jakta.parseStruct("N > M & S is N - 1"),
                    goals =
                    listOf(
                        Print(Jakta.parseStruct("print(\"Down\", N)")),
                        Achieve(Jakta.parseStruct("count(S, M, down)")),
                    ),
                ),
            ),
        )

    Mas
        .of(
            ExecutionStrategy
                .oneThreadPerAgent(),
            BasicEnvironment(),
            agent,
        ).start()
}
