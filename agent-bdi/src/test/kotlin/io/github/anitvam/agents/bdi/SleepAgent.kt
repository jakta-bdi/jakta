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
    val start = Jacop.parseStruct("start")
    val sleepingAgent = Agent.of(
        name = "Sleeping Agent",
        beliefBase = BeliefBase.of(Belief.fromSelfSource(Jacop.parseStruct("run"))),
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("start"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("print(\"Before Sleep\")")),
                    ActInternally.of(Jacop.parseStruct("sleep(5000)")),
                    ActInternally.of(Jacop.parseStruct("print(\"After Sleep\")")),
                    ActInternally.of(Jacop.parseStruct("stop")),
                ),
            ),
        ),
    )
    Mas.of(ExecutionStrategy.oneThreadPerMas(), Environment.of(), sleepingAgent).start()
}
