package it.unibo.jakta

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.executionstrategies.setTimeDistribution
import it.unibo.jakta.fsm.time.SimulatedTime
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution

object TestDiscreteEventExecution : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ASActionContext) {
        println("time: ${context.invocationTimestamp}")
    }
}

fun main() {
    val alice =
        ASAgent
            .of(
                events = listOf(AchievementGoalInvocation(Atom.of("time"))),
                planLibrary =
                mutableListOf(
                    ASNewPlan.ofAchievementGoalInvocation(
                        value = Jakta.parseStruct("time"),
                        goals =
                        listOf(
                            TestDiscreteEventExecution,
                            Achieve(Jakta.parseStruct("time")),
                        ),
                    ),
                ),
            ).setTimeDistribution { Time.continuous((it as SimulatedTime).value + 5.0) }

    Mas
        .of(
            ExecutionStrategy
                .discreteEventExecution(),
            BasicEnvironment(),
            alice,
        ).start()
}
