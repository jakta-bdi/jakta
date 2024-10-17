package it.unibo.jakta

import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.Event
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val agent = Agent.of(
        name = "agent",
        events = listOf(
            Event.of(AchievementGoalInvocation(Jakta.parseStruct("count(0, 10, up)"))),
            Event.of(AchievementGoalInvocation(Jakta.parseStruct("count(100, 90, down)"))),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("count(N, N, Dir)"),
                goals = listOf(ActInternally.of(Jakta.parseStruct("print(\"End of\", Dir)"))),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("count(N, M, up)"),
                guard = Jakta.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Up\", N)")),
                    Achieve.of(Jakta.parseStruct("count(S, M, up)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("count(N, M, down)"),
                guard = Jakta.parseStruct("N > M & S is N - 1"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Down\", N)")),
                    Achieve.of(Jakta.parseStruct("count(S, M, down)")),
                ),
            ),
        ),
    )

    val env = Environment.of()
    Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), env, agent).start()
}
