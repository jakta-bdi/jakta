package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

fun main() {
    val alice =
        Agent.of(
            name = "Alice",
            events =
                listOf(
                    Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("my_thread"))),
                ),
            planLibrary =
                PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = Jakta.parseStruct("my_thread"),
                        goals =
                            listOf(
                                ActInternally.of(Jakta.parseStruct("thread")),
                            ),
                    ),
                ),
            internalActions =
                mapOf(
                    "thread" to
                        object : AbstractInternalAction("thread", 0) {
                            override fun action(request: InternalRequest) {
                                println("Thread: ${Thread.currentThread().name}")
                            }
                        },
                ),
        )
    val environment = Environment.of()

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, alice, alice.copy()).start()
}
