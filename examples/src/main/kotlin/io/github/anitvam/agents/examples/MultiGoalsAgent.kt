package io.github.anitvam.agents.examples

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.JasonParser
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.AchievementGoalInvocation
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {

    val agent = Agent.of(
        name = "agent",
        events = listOf(
            Event.of(AchievementGoalInvocation(JasonParser.parser.parseStruct("count(0, 10, up)"))),
            Event.of(AchievementGoalInvocation(JasonParser.parser.parseStruct("count(100, 90, down)"))),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("count(N, N, Dir)"),
                goals = listOf(ActInternally.of(JasonParser.parser.parseStruct("print(\"End of\", Dir)")))
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("count(N, M, up)"),
                guard = JasonParser.parser.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Up\", N)")),
                    Achieve.of(JasonParser.parser.parseStruct("count(S, M, up)")),
                )
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("count(N, M, down)"),
                guard = JasonParser.parser.parseStruct("N > M & S is N - 1"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"Down\", N)")),
                    Achieve.of(JasonParser.parser.parseStruct("count(S, M, down)")),
                )
            ),
        )
    )

    val env = Environment.of()
    Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, agent).start()
}
