package it.unibo.jakta

import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.perception.Perception
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Substitution
import java.util.ArrayDeque

class PrintThread: AbstractExecutionAction("PrintThread", 0) {
    override fun applySubstitution(substitution: Substitution) = Unit
    override fun invoke(context: ActionInvocationContext): List<SideEffect> {
        println("Thread: ${Thread.currentThread().name}")
        return emptyList()
    }
}


fun agentGenerator(name: String) = ASAgent.of(
    name = name,
    events = ArrayDeque(
        listOf(AchievementGoalInvocation(Jakta.parseStruct("my_thread"))),
    ),
    planLibrary = mutableListOf(ASPlan.ofAchievementGoalInvocation(
        value = Jakta.parseStruct("my_thread"),
        goals = listOf(PrintThread()),
    )),
)


fun main() {
    val environment = BasicEnvironment(true, Perception.empty())

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
        environment,
        agentGenerator("alice"),
        agentGenerator("bob"),
    ).start()
}
