package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.actions.impl.AbstractInternalAction
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

fun main() {
    val alice = Agent.of(
        name = "Alice",
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(Jacop.parseStruct("my_thread")))
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jacop.parseStruct("my_thread"),
                goals = listOf(
                    ActInternally.of(Jacop.parseStruct("thread"))
                )
            )
        ),
        internalActions = mapOf(
            "thread" to object : AbstractInternalAction("thread", 0) {
                override fun action(request: InternalRequest) {
                    println("Thread: ${Thread.currentThread().name}")
                }
            }
        )
    )
    val environment = Environment.of()

    Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, alice, alice.copy()).start()
}
