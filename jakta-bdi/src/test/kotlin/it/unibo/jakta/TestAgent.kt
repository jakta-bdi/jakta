package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.actions.InternalActions
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.impl.AbstractInternalAction
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.goals.AddBelief
import it.unibo.jakta.goals.Test
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Var

class TestAgent : DescribeSpec({
    @Suppress("VariableNaming")
    val TEST = object : AbstractInternalAction("test", 2) {
        override suspend fun action(request: InternalRequest) {
            if (request.arguments[0].isAtom && request.arguments[1].isAtom) {
                val first: Int = Integer.parseInt(request.arguments[0].castToAtom().value)
                val second: Int = Integer.parseInt(request.arguments[1].castToAtom().value)
                first shouldBe second
            } else {
                fail("Wrong Arguments for test action")
            }
        }
    }

    @Suppress("VariableNaming")
    val FAILTEST = object : AbstractInternalAction("failtest", 0) {
        override suspend fun action(request: InternalRequest) {
            fail("This action should not be executed by the agent")
        }
    }

    @Suppress("VariableNaming")
    val PASSTEST = object : AbstractInternalAction("passtest", 0) {
        override suspend fun action(request: InternalRequest) {
            println("This should be shown")
        }
    }
    val start = Atom.of("start")
    val agent = ASAgent.of(
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        internalActions = InternalActions.default() +
            (TEST.signature.name to TEST) +
            (FAILTEST.signature.name to FAILTEST) +
            (PASSTEST.signature.name to PASSTEST),
    )

    val environment = Environment.of()

    describe("An agent") {
        it("should call an internal action specifying its name") {
            val newAgent = agent.copy(
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(ActInternally.of(Struct.of("test", Atom.of("5"), Atom.of("5")))),
                    ),
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
            val INCREMENT = object : AbstractInternalAction("increment", 2) {
                override suspend fun action(request: InternalRequest) {
                    val first: Atom = request.arguments.first().castToAtom()
                    val second: Var = request.arguments[1].castToVar()
                    val computation = Atom.of((Integer.parseInt(first.value) + 1).toString())
                    result = Substitution.of(second to computation)
                }
            }

            @Suppress("VariableNaming")
            val X = Var.of("X")
            val newAgent = agent.copy(
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("increment", Atom.of("5"), X)),
                            ActInternally.of(Struct.of("test", X, Atom.of("6"))),
                        ),
                    ),
                ),
                internalActions = agent.context.internalActions + (INCREMENT.signature.name to INCREMENT),
            )
            Mas.of(
                it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(),
                environment,
                newAgent,
            ).start()
        }
        it("can fail its intention using the internal action fail") {
            val newAgent = agent.copy(
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("fail")),
                            ActInternally.of(Struct.of("failtest")),
                        ),
                    ),
                    Plan.ofAchievementGoalFailure(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("passtest")),
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
        it("fails its intention if the action to execute is not found") {
            val newAgent = agent.copy(
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("nonexistingaction")),
                            ActInternally.of(Atom.of("failtest")),
                        ),
                    ),
                    Plan.ofAchievementGoalFailure(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("passtest")),
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
        it("can modify agent's context declaring custom actions") {
            val needChocolate = Belief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

            @Suppress("VariableNaming")
            val ADDBELIEF = object : AbstractInternalAction("add_belief", 1) {
                override suspend fun action(request: InternalRequest) {
                    AddBelief.of(needChocolate)
                }
            }
            val newAgent = agent.copy(
                internalActions = agent.context.internalActions + (ADDBELIEF.signature.name to ADDBELIEF),
                planLibrary = PlanLibrary.of(
                    Plan.ofAchievementGoalInvocation(
                        value = start,
                        goals = listOf(
                            ActInternally.of(Struct.of("add_belief")),
                        ),
                    ),
                    Plan.ofBeliefBaseAddition(
                        belief = needChocolate,
                        goals = listOf(
                            Test.of(needChocolate),
                            ActInternally.of(Struct.of("passtests")),
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

fun main() {
    val needChocolate = Belief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))

    @Suppress("VariableNaming")
    val ADDBELIEF = object : AbstractInternalAction("add_belief", 1) {
        override suspend fun action(request: InternalRequest) {
            addBelief(Belief.from(request.arguments.first().castToRule()))
        }
    }
    val start = Atom.of("start")
    val agent = ASAgent.of(
        events = listOf(Event.ofAchievementGoalInvocation(Achieve.of(start))),
        internalActions = InternalActions.default() + (ADDBELIEF.signature.name to ADDBELIEF),
        planLibrary = PlanLibrary.of(
            Plan.ofAchievementGoalInvocation(
                value = start,
                goals = listOf(
                    ActInternally.of(Struct.of("add_belief", needChocolate.rule)),
                ),
            ),
            Plan.ofBeliefBaseAddition(
                belief = needChocolate,
                goals = listOf(
                    Test.of(needChocolate),
                    ActInternally.of(Struct.of("print", Atom.of("guacamole"))),
                ),
            ),
        ),
    )
    Mas.of(it.unibo.jakta.executionstrategies.ExecutionStrategy.oneThreadPerAgent(), Environment.of(), agent).start()
}
