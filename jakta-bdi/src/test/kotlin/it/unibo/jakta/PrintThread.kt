package it.unibo.jakta

import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.perception.Perception
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Substitution

object PrintThread : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this
    override fun execute(context: ActionInvocationContext) {
        println("[${context.agentContext.agentName}]Thread: ${Thread.currentThread().name}")
    }
}

fun agentGenerator(name: String) = ASAgent.of(
    name = name,
    events = listOf(AchievementGoalInvocation(Jakta.parseStruct("my_thread"))),
    planLibrary = mutableListOf(
        ASPlan.ofAchievementGoalInvocation(
            value = Jakta.parseStruct("my_thread"),
            goals = listOf(PrintThread),
        ),
    ),
)

fun main() {
    val environment = BasicEnvironment(false, Perception.empty())

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        environment,
        agentGenerator("alice"),
        agentGenerator("bob"),
    ).start()
}
