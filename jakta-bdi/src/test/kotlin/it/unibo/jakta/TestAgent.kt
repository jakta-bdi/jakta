package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.actions.stdlib.Fail
import it.unibo.jakta.actions.stdlib.Test
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.plans.impl.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

data class IncrementAction(val first: Int, val second: Var) : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = IncrementAction(first, second)

    override fun execute(context: ASActionContext) {
        val computation = Atom.of((first + 1).toString())
        result = Substitution.of(second to computation)
    }
}

data class AddBeliefAction(val belief: ASBelief) : AbstractAction() {
    override fun applySubstitution(substitution: Substitution) = AddBeliefAction(belief)

    override fun invoke(context: ASActionContext): List<SideEffect> = listOf(BeliefChange.BeliefAddition(belief))
}

data class TestAction(var first: Term, var second: Term) : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = TestAction(
        first = substitution.applyTo(first) ?: error("first parameter cannot be substituted."),
        second = substitution.applyTo(second) ?: error("second parameter cannot be substituted."),
    )

    override fun execute(context: ASActionContext) {
        first shouldBe second
    }
}

object FailTestAction : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = FailTestAction

    override fun execute(context: ASActionContext) {
        fail("This action should not be executed by the agent")
    }
}

object PassTestAction : AbstractAction.WithoutSideEffects() {
    override fun applySubstitution(substitution: Substitution) = this

    override fun execute(context: ASActionContext) {
        println("This should be shown")
    }
}

class TestAgent :
    DescribeSpec({

        val start = Atom.of("start")

        fun agentGenerator(plans: MutableCollection<ASPlan>) = ASAgent.of(
            events = listOf(AchievementGoalInvocation(start)),
            planLibrary = plans.toList().toMutableList(),
        )

        val environment = BasicEnvironment()

        describe("An agent") {
            it("should call an internal action specifying its name") {
                val newAgent =
                    agentGenerator(
                        mutableListOf(
                            ASNewPlan.ofAchievementGoalInvocation(
                                start,
                                listOf(TestAction(Atom.of("5"), Atom.of("5"))),
                            ),
                        ),
                    )

                Mas
                    .of(
                        ExecutionStrategy
                            .oneThreadPerAgent(),
                        environment,
                        newAgent,
                    ).start()
            }
            it("can declare custom actions to be performed") {

                val x = Var.of("X")
                val newAgent =
                    agentGenerator(
                        mutableListOf(
                            ASNewPlan.ofAchievementGoalInvocation(
                                value = start,
                                goals = listOf(IncrementAction(5, x), TestAction(x, Atom.of("6"))),
                            ),
                        ),
                    )
                Mas
                    .of(
                        ExecutionStrategy
                            .oneThreadPerAgent(),
                        environment,
                        newAgent,
                    ).start()
            }
            it("can fail its intention using the internal action fail") {
                val newAgent =
                    agentGenerator(
                        mutableListOf(
                            ASNewPlan.ofAchievementGoalInvocation(start, listOf(Fail, FailTestAction)),
                            ASNewPlan.ofAchievementGoalFailure(start, listOf(PassTestAction)),
                        ),
                    )
                Mas
                    .of(
                        ExecutionStrategy
                            .oneThreadPerAgent(),
                        environment,
                        newAgent,
                    ).start()
            }
            it("fails its intention if the action to execute is not found") {
                val newAgent =
                    agentGenerator(
                        mutableListOf(
                            ASNewPlan.ofAchievementGoalInvocation(
                                value = start,
                                goals =
                                listOf(
                                    // NOTE: with the new version of action a non-existing action cannot be put anymore.
                                    // ActInternally.of(Struct.of("nonexistingaction")),
                                    // ActInternally.of(Atom.of("failtest")),
                                ),
                            ),
                            ASNewPlan.ofAchievementGoalFailure(start, listOf(PassTestAction)),
                        ),
                    )
                Mas
                    .of(
                        ExecutionStrategy
                            .oneThreadPerAgent(),
                        environment,
                        newAgent,
                    ).start()
            }
            it("can modify agent's context declaring custom actions") {
                val needChocolate = ASBelief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

                val newAgent =
                    agentGenerator(
                        mutableListOf(
                            ASNewPlan.ofAchievementGoalInvocation(
                                value = start,
                                goals = listOf(AddBeliefAction(needChocolate)),
                            ),
                            ASNewPlan.ofBeliefBaseAddition(
                                belief = needChocolate,
                                goals =
                                listOf(
                                    Test(needChocolate.content),
                                    PassTestAction,
                                ),
                            ),
                        ),
                    )
                Mas
                    .of(
                        ExecutionStrategy
                            .oneThreadPerAgent(),
                        environment,
                        newAgent,
                    ).start()
            }
        }
    })
