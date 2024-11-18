package it.unibo.jakta

import it.unibo.jakta.environment.Environment
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val env = Environment.of()

    val start = it.unibo.jakta.Jakta.parseStruct("start(0, 10)")
    val alice = it.unibo.jakta.ASAgent.Companion.of(
        name = "alice",
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = it.unibo.jakta.Jakta.parseStruct("start(N, N)"),
                goals = listOf(
                    ActInternally.of(it.unibo.jakta.Jakta.parseStruct("print(\"hello world\", N)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = it.unibo.jakta.Jakta.parseStruct("start(N, M)"),
                guard = it.unibo.jakta.Jakta.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(it.unibo.jakta.Jakta.parseStruct("print(\"hello world\", N)")),
                    Achieve.of(it.unibo.jakta.Jakta.parseStruct("start(S, M)")),
                ),
            ),
        ),
    )

    val bob = it.unibo.jakta.ASAgent.Companion.of(
        name = "bob",
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = start,
                goals = listOf(
                    ActInternally.of(it.unibo.jakta.Jakta.parseStruct("print('Hello, my name is Bob')")),
                ),
            ),
        ),
    )

    val mas = it.unibo.jakta.Mas.Companion.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        env,
        alice,
        bob,
    )

    mas.start()
}
