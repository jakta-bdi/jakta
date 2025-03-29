package it.unibo.jakta.agents.bdi

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.actions.InternalActions
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.AddBelief
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

class TestAgent :
    DescribeSpec({
        val testInternalAction =
            object : AbstractInternalAction("test", 2) {
                override fun action(request: InternalRequest) {
                    if (request.arguments[0].isAtom && request.arguments[1].isAtom) {
                        val first: Int = Integer.parseInt(request.arguments[0].castToAtom().value)
                        val second: Int = Integer.parseInt(request.arguments[1].castToAtom().value)
                        first shouldBe second
                    } else {
                        fail("Wrong Arguments for test action")
                    }
                }
            }

        val failingTestInternalAction =
            object : AbstractInternalAction("failtest", 0) {
                override fun action(request: InternalRequest) {
                    fail("This action should not be executed by the agent")
                }
            }

        val passingTestInternalAction =
            object : AbstractInternalAction("passtest", 0) {
                override fun action(request: InternalRequest) {
                    println("This should be shown")
                }
            }
        val start = Atom.of("start")
        val agent =
            Agent.of(
                events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
                internalActions =
                    InternalActions.default() +
                        (testInternalAction.signature.name to testInternalAction) +
                        (failingTestInternalAction.signature.name to failingTestInternalAction) +
                        (passingTestInternalAction.signature.name to passingTestInternalAction),
            )

        val environment = Environment.of()

        describe("An agent") {
            it("should call an internal action specifying its name") {
                val newAgent =
                    agent.copy(
                        planLibrary =
                            PlanLibrary.of(
                                Plan.ofAchievementGoalInvocation(
                                    value = start,
                                    goals = listOf(ActInternally.of(Struct.of("test", Atom.of("5"), Atom.of("5")))),
                                ),
                            ),
                    )
                Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, newAgent).start()
            }
            it("can declare custom actions to be performed") {
                val incrementingInternalAction =
                    object : AbstractInternalAction("increment", 2) {
                        override fun action(request: InternalRequest) {
                            val first: Atom = request.arguments.first().castToAtom()
                            val second: Var = request.arguments[1].castToVar()
                            val computation = Atom.of((Integer.parseInt(first.value) + 1).toString())
                            result = Substitution.of(second to computation)
                        }
                    }

                val variableX = Var.of("X")
                val newAgent =
                    agent.copy(
                        planLibrary =
                            PlanLibrary.of(
                                Plan.ofAchievementGoalInvocation(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(
                                                Struct.of("increment", Atom.of("5"), variableX),
                                            ),
                                            ActInternally.of(
                                                Struct.of("test", variableX, Atom.of("6")),
                                            ),
                                        ),
                                ),
                            ),
                        internalActions =
                            agent.context.internalActions +
                                (incrementingInternalAction.signature.name to incrementingInternalAction),
                    )
                Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, newAgent).start()
            }
            it("can fail its intention using the internal action fail") {
                val newAgent =
                    agent.copy(
                        planLibrary =
                            PlanLibrary.of(
                                Plan.ofAchievementGoalInvocation(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(Struct.of("fail")),
                                            ActInternally.of(Struct.of("failtest")),
                                        ),
                                ),
                                Plan.ofAchievementGoalFailure(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(Struct.of("passtest")),
                                        ),
                                ),
                            ),
                    )
                Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, newAgent).start()
            }
            it("fails its intention if the action to execute is not found") {
                val newAgent =
                    agent.copy(
                        planLibrary =
                            PlanLibrary.of(
                                Plan.ofAchievementGoalInvocation(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(Struct.of("nonexistingaction")),
                                            ActInternally.of(Atom.of("failtest")),
                                        ),
                                ),
                                Plan.ofAchievementGoalFailure(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(Struct.of("passtest")),
                                        ),
                                ),
                            ),
                    )
                Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, newAgent).start()
            }
            it("can modify agent's context declaring custom actions") {
                val needChocolate = Belief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

                val beliefToAdd =
                    object : AbstractInternalAction("add_belief", 1) {
                        override fun action(request: InternalRequest) {
                            AddBelief.of(needChocolate)
                        }
                    }
                val newAgent =
                    agent.copy(
                        internalActions =
                            agent.context.internalActions + (beliefToAdd.signature.name to beliefToAdd),
                        planLibrary =
                            PlanLibrary.of(
                                Plan.ofAchievementGoalInvocation(
                                    value = start,
                                    goals =
                                        listOf(
                                            ActInternally.of(Struct.of("add_belief")),
                                        ),
                                ),
                                Plan.ofBeliefBaseAddition(
                                    belief = needChocolate,
                                    goals =
                                        listOf(
                                            Test.of(needChocolate),
                                            ActInternally.of(Struct.of("passtests")),
                                        ),
                                ),
                            ),
                    )
                Mas.of(ExecutionStrategy.oneThreadPerAgent(), environment, newAgent).start()
            }
        }
    })

fun main() {
    val needChocolate = Belief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

    val beliefToAdd =
        object : AbstractInternalAction("add_belief", 1) {
            override fun action(request: InternalRequest) {
                addBelief(Belief.from(request.arguments.first().castToRule()))
            }
        }
    val start = Atom.of("start")
    val agent =
        Agent.of(
            events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
            internalActions = InternalActions.default() + (beliefToAdd.signature.name to beliefToAdd),
            planLibrary =
                PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals =
                            listOf(
                                ActInternally.of(Struct.of("add_belief", needChocolate.rule)),
                            ),
                    ),
                    Plan.ofBeliefBaseAddition(
                        belief = needChocolate,
                        goals =
                            listOf(
                                Test.of(needChocolate),
                                ActInternally.of(Struct.of("print", Atom.of("guacamole"))),
                            ),
                    ),
                ),
        )
    Mas.of(ExecutionStrategy.oneThreadPerAgent(), Environment.of(), agent).start()
}
