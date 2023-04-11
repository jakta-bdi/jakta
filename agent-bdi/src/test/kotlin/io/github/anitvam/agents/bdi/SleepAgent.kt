package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {
    val start = Jakta.parseStruct("start")
    val sleepingAgent = Agent.of(
        name = "Sleeping Agent",
        beliefBase = BeliefBase.of(Belief.fromSelfSource(Jakta.parseStruct("run"))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(\"Before Sleep\")")),
                    ActInternally.of(Jakta.parseStruct("sleep(5000)")),
                    ActInternally.of(Jakta.parseStruct("print(\"After Sleep\")")),
                    ActInternally.of(Jakta.parseStruct("stop")),
                ),
            ),
        ),
    )
    Mas.of(ExecutionStrategy.oneThreadPerMas(), Environment.of(), sleepingAgent).start()
}
