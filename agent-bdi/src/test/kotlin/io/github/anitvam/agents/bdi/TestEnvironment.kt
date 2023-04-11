package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy

fun main() {
    val env = Environment.of()

    val start = Jakta.parseStruct("start(0, 10)")
    val alice = Agent.of(
        name = "alice",
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(N, N)"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"hello world\", N)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(N, M)"),
                guard = Jakta.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"hello world\", N)")),
                    Achieve.of(Jakta.parseStruct("start(S, M)")),
                ),
            ),
        ),
    )

    val bob = Agent.of(
        name = "bob",
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = start,
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print('Hello, my name is Bob')")),
                ),
            ),
        ),
    )

    val mas = Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, alice, bob)

    mas.start()
}
