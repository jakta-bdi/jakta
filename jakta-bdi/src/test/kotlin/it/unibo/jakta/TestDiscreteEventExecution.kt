package it.unibo.jakta

import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.impl.AbstractInternalAction
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.executionstrategies.setTimeDistribution
import it.unibo.jakta.fsm.time.SimulatedTime
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val dummyAction = object : AbstractInternalAction("time", 0) {
        override suspend fun action(request: InternalRequest) {
            println("time: ${request.requestTimestamp}")
        }
    }
    val alice = ASAgent.of(
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("time")))),
        internalActions = InternalActions.default() + (dummyAction.signature.name to dummyAction),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("time"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("time")),
                    Achieve.of(Jakta.parseStruct("time")),
                ),
            ),
        ),
    ).setTimeDistribution { Time.continuous((it as SimulatedTime).value + 5.0) }

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.discreteEventExecution(),
        Environment.of(),
        alice,
    ).start()
}
