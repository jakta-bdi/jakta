package it.unibo.jakta

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.events.Event
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val start = Jakta.parseStruct("start")
    val sleepingAgent = Agent.of(
        name = "Sleeping Agent",
        beliefBase = PrologBeliefBase.of(Belief.fromSelfSource(Jakta.parseStruct("run"))),
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
    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerMas(),
        Environment.of(),
        sleepingAgent,
    ).start()
}
