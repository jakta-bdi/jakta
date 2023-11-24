package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

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
    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, agent).start()
}
