package it.unibo.jakta

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.perception.Perception
import it.unibo.tuprolog.core.Substitution

object PrintThread : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ASActionContext) {
        println("[${context.agentContext.agentName}]Thread: ${Thread.currentThread().name}")
    }
}

fun agentGenerator(name: String) = ASAgent.of(
    name = name,
    events = listOf(AchievementGoalInvocation(Jakta.parseStruct("my_thread"))),
    planLibrary =
    mutableListOf(
        ASNewPlan.ofAchievementGoalInvocation(
            value = Jakta.parseStruct("my_thread"),
            goals = listOf(PrintThread),
        ),
    ),
)

fun main() {
    val environment = BasicEnvironment(false, Perception.empty())

    Mas
        .of(
            ExecutionStrategy
                .oneThreadPerAgent(),
            environment,
            agentGenerator("alice"),
            agentGenerator("bob"),
        ).start()
}
