package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.executionstrategies.setTimeDistribution
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.github.anitvam.agents.fsm.time.SimulatedTime
import io.github.anitvam.agents.fsm.time.Time

fun main() {

    val dummyAction = object : InternalAction("time", 0) {
        override fun InternalRequest.action() {
            println("time: $requestTimestamp}")
        }
    }
    val alice = Agent.of(
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(Jacop.parseStruct("time")))),
        internalActions = InternalActions.default() + (dummyAction.signature.name to dummyAction),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("time"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("time")),
                    Achieve.of(Jacop.parseStruct("time"))
                ),
            ),
        ),
    ).setTimeDistribution { Time.continuous((it as SimulatedTime).value + 5.0) }

    Mas.of(ExecutionStrategy.discreteEventExecution(), Environment.of(), alice).start()
}
