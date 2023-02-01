package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy

fun main() {
    val env = Environment.of()

    val start = Jacop.parseStruct("start(0, 10)")
    val alice = Agent.of(
        name = "alice",
        beliefBase = BeliefBase.of(listOf(Belief.fromSelfSource(Jacop.parseStruct("run")))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("start(N, N)"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"hello world\", N)")),
                ),
            ),
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("start(N, M)"),
                guard = Jacop.parseStruct("N < M & S is N + 1"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"hello world\", N)")),
                    Achieve.of(Jacop.parseStruct("start(S, M)")),
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
                    ActInternally.of(Jacop.parseStruct("print('Hello, my name is Bob')")),
                ),
            ),
        ),
    )

    val mas = Mas.of(ExecutionStrategy.oneThreadPerAgent(), env, alice, bob)

    mas.start()
}
