package it.unibo.jakta

import io.kotest.assertions.fail
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.actions.stdlib.Stop
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import org.gciatto.kt.math.BigInteger
import kotlin.system.exitProcess

class TestPlans : DescribeSpec({
    val strawberryDesire = ASBelief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
    val chocolateNeed = ASBelief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))
    val genericDesire = ASBelief.fromSelfSource(Struct.of("desire", Jakta.parseVar("X")))

    val plan = ASPlan.ofBeliefBaseAddition(genericDesire, emptyList())
    val planLibrary = listOf(plan)

    data class VerifySubstitution(
        val term: Term,
        val expected: Int,
    ) : AbstractExecutionAction.WithoutSideEffects() {
        override fun applySubstitution(substitution: Substitution) = VerifySubstitution(
            term = substitution.applyTo(term) ?: fail("Failed to apply substitution to $term"),
            expected = expected,
        )

        override fun execute(context: ActionInvocationContext) {
            println("The term is $term")
            term.asInteger()?.value shouldBe BigInteger.of(expected)
        }
    }

    describe("A Plan") {
        it("should be relevant if its trigger is unified with the event value") {
            val event = BeliefBaseAddition(strawberryDesire)
            planLibrary.size shouldBe 1
            planLibrary.first() shouldBe plan

            val relevantPlans = planLibrary.filter {
                it.isRelevant(event)
            }

            relevantPlans.size shouldBe 1
            relevantPlans.first() shouldBe plan

            val planLibrary2 = listOf(ASPlan.ofBeliefBaseRemoval(genericDesire, emptyList()))
            planLibrary2.count {
                it.isRelevant(event)
            } shouldBe 0
        }

        it("should be applicable if its trigger is a valid Predicate") {
            val event = BeliefBaseAddition(strawberryDesire)

            val bb = ASMutableBeliefBase.of(listOf(chocolateNeed))

            plan.isApplicable(event, bb) shouldBe true

            val plan2 = ASPlan.ofBeliefBaseAddition(
                belief = genericDesire,
                guard = Truth.FALSE,
                goals = emptyList(),
            )
            plan2.isApplicable(event, bb) shouldBe false

            val plan3 = ASPlan.ofBeliefBaseRemoval(genericDesire, emptyList())
            plan3.isApplicable(event, bb) shouldBe false
        }

        it("should unify the triggering event variables") {
            val action = Print(Jakta.parseVar("S"))
            val event = AchievementGoalInvocation(Jakta.parseStruct("start(0, 10)"))
            val p = ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, M)"),
                goals = listOf(action),
            )
            p.isApplicable(event, ASMutableBeliefBase.of()) shouldBe true
            val ap = p.applicablePlan(event, ASMutableBeliefBase.of())
            ap.trigger.value shouldBe event.value
            ap.apply(event).first().shouldBeInstanceOf<Print>()
        }

        it("should run if and only if the context is valid, and unify those values") {
            val event = AchievementGoalInvocation(Jakta.parseStruct("pippo(0)"))
            val p = ASPlan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("pippo(S)"),
                guard = Jakta.parseStruct("S < 5 & N is S + 1"),
                goals = listOf(
                    VerifySubstitution(Jakta.parseVar("S"), 0),
                    VerifySubstitution(Jakta.parseVar("N"), 1),
                ),
            )
            val ap = p.applicablePlan(event, ASMutableBeliefBase.of())
            ap.apply(event).first().invoke(ActionRequest.of(ASAgent.of().context, Time.real()))
            // ap.apply(event).first() shouldBe Atom.of("0")
            // ap.apply(event)[1] shouldBe Atom.of("1")
        }
    }

    describe("Plan recursion") {
        it("should unify with different values") {
            val start = Jakta.parseStruct("start(0, 1)")
            val alice = ASAgent.of(
                name = "alice",
                events = listOf(AchievementGoalInvocation(start)),
                planLibrary = mutableListOf(
                    ASPlan.ofAchievementGoalInvocation(
                        value = Jakta.parseStruct("start(S, S)"),
                        goals = listOf(
                            VerifySubstitution(Jakta.parseVar("S"), 1),
                            Stop,
                        ),
                    ),
                    ASPlan.ofAchievementGoalInvocation(
                        value = Jakta.parseStruct("start(S, M)"),
                        guard = Jakta.parseStruct("S < M & N is S + 1"),
                        goals = listOf(
                            VerifySubstitution(Jakta.parseVar("S"), 0),
                            Achieve(Jakta.parseStruct("start(N, M)")),
                        ),
                    ),
                ),
            )

            alice.controller = object : Activity.Controller {
                override fun restart() = TODO("Not yet implemented")
                override fun pause() = TODO("Not yet implemented")
                override fun resume() = TODO("Not yet implemented")
                override fun stop() = exitProcess(0)
                override fun currentTime(): Time = Time.real()
                override fun sleep(millis: Long) = TODO("Not yet implemented")
            }
            while (true) {
                ASAgentLifecycle.of(
                    alice,
                    BasicEnvironment(debugEnabled = false),
                ).runOneCycle()
            }
        }
    }
})
