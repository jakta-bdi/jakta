package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy

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
