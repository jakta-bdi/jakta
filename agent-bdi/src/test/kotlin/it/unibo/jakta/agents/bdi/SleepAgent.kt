package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

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
