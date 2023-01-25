package io.github.anitvam.agents.examples

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.JasonParser
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.OneThreadPerAgent
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {

    val env = Environment.of()

    val start = JasonParser.parser.parseStruct("start(0, 10)")

    val alice = Agent.of(
        name = "alice",
        beliefBase = BeliefBase.of(listOf(Belief.fromSelfSource(JasonParser.parser.parseStruct("run")))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("start(N, N)"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"hello world\", N)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = JasonParser.parser.parseStruct("start(N, M)"),
                guard = JasonParser.parser.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(JasonParser.parser.parseStruct("print(\"hello world\", N)")),
                    Achieve.of(JasonParser.parser.parseStruct("start(S, M)")),
                ),
            ),
        ),
    )

    val mas = Mas.of(OneThreadPerAgent(), env, alice)

    mas.start()
}
