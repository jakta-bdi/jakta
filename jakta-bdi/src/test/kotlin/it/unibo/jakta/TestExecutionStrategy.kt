package it.unibo.jakta

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

fun main() {
    val alice = ASAgent.of(
        name = "Alice",
        events = listOf(
            Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("my_thread"))),
        ),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("my_thread"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("thread")),
                ),
            ),
        ),
        internalActions = mapOf(
            "thread" to object : AbstractInternalAction("thread", 0) {
                override suspend fun action(request: InternalRequest) {
                    println("Thread: ${Thread.currentThread().name}")
                }
            },
        ),
    )
    val environment = Environment.of()

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        environment,
        alice,
        alice.copy(),
    ).start()
}
