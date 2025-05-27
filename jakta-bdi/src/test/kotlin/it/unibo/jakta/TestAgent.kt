package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Fail
import it.unibo.jakta.actions.stdlib.Test
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

data class IncrementAction(
    val first: Int,
    val second: Var,
) : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = IncrementAction(first, second)

    override fun execute(context: ActionInvocationContext) {
        val computation = Atom.of((first + 1).toString())
        result = Substitution.of(second to computation)
    }
}

data class AddBeliefAction(
    val belief: ASBelief,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution) = AddBeliefAction(belief)

    override fun invoke(context: ActionInvocationContext): List<SideEffect> =
        listOf(BeliefChange.BeliefAddition(belief))
}

data class TestAction(
    var first: Term,
    var second: Term,
) : AbstractExecutionAction.WithoutSideEffects() {

    override fun applySubstitution(substitution: Substitution) = TestAction(
        first = substitution.applyTo(first) ?: error("first parameter cannot be substituted."),
        second = substitution.applyTo(second) ?: error("second parameter cannot be substituted."),
    )

    override fun execute(context: ActionInvocationContext) {
        first shouldBe second
    }
}

object FailTestAction : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = FailTestAction

    override fun execute(context: ActionInvocationContext) {
        fail("This action should not be executed by the agent")
    }
}

object PassTestAction : AbstractExecutionAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ActionInvocationContext) {
        println("This should be shown")
    }
}

class TestAgent : DescribeSpec({

    val start = Atom.of("start")

    fun agentGenerator(plans: MutableCollection<ASPlan>) = ASAgent.of(
        events = listOf(AchievementGoalInvocation(start)),
        planLibrary = plans,
    )

    val environment = BasicEnvironment()

    describe("An agent") {
        it("should call an internal action specifying its name") {
            val newAgent = agentGenerator(
                mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(start, listOf(TestAction(Atom.of("5"), Atom.of("5")))),
                ),
            )

            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
        it("can declare custom actions to be performed") {

            @Suppress("VariableNaming")
            val X = Var.of("X")
            val newAgent = agentGenerator(
                mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(IncrementAction(5, X), TestAction(X, Atom.of("6"))),
                    ),
                ),
            )
            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
        it("can fail its intention using the internal action fail") {
            val newAgent = agentGenerator(
                mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(start, listOf(Fail, FailTestAction)),
                    ASPlan.ofAchievementGoalFailure(start, listOf(PassTestAction)),
                ),
            )
            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
        it("fails its intention if the action to execute is not found") {
            val newAgent = agentGenerator(
                mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            // NOTE: with the new version of action a non-existing action cannot be put anymore.
                            // ActInternally.of(Struct.of("nonexistingaction")),
                            // ActInternally.of(Atom.of("failtest")),
                        ),
                    ),
                    ASPlan.ofAchievementGoalFailure(start, listOf(PassTestAction)),
                ),
            )
            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
        it("can modify agent's context declaring custom actions") {
            val needChocolate = ASBelief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

            val newAgent = agentGenerator(
                mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(AddBeliefAction(needChocolate)),
                    ),
                    ASPlan.ofBeliefBaseAddition(
                        belief = needChocolate,
                        goals = listOf(
                            Test(needChocolate.content),
                            PassTestAction,
                        ),
                    ),
                ),
            )
            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
    }
})
