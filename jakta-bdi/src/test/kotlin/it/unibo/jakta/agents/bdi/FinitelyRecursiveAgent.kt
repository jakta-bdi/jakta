package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

fun main() {
    val env = Environment.of()

    val start = Jakta.parseStruct("start(0, 10)")

    val alice = Agent.of(
        name = "alice",
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, S)"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"hello world\", S)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, M)"),
                guard = Jakta.parseStruct("S < M & N is S + 1"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"hello world\", S)")),
                    Achieve.of(Jakta.parseStruct("start(N, M)")),
                ),
            ),
        ),
    )

    val mas = Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, alice)
    // println(Jakta.printAslSyntax(alice))
    mas.start()
}
