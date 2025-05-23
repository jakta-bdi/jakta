package it.unibo.jakta

import io.kotest.assertions.retry
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.setTimeDistribution
import it.unibo.jakta.fsm.time.SimulatedTime
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution

fun main() {
    class DummyAction: AbstractExecutionAction("DummyAction", 0) {
        override fun applySubstitution(substitution: Substitution) = Unit

        override fun invoke(context: ActionInvocationContext): List<SideEffect> {
            println("time: ${context.invocationTimestamp}")
            return emptyList()
        }

    }

    val alice = ASAgent.of(
        events = listOf(AchievementGoalInvocation(Atom.of("time"))),
        planLibrary = mutableListOf(
            ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("time"),
                goals = listOf(
                    DummyAction(),
                    Achieve(Jakta.parseStruct("time")),
                ),
            ),
        ),
    ).setTimeDistribution { Time.continuous((it as SimulatedTime).value + 5.0) }

    Mas.of(
        it.unibo.jakta.executionstrategies.ExecutionStrategy.discreteEventExecution(),
        BasicEnvironment(),
        alice,
    ).start()
}
