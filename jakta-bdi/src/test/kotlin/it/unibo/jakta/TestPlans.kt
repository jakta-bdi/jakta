package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.stdlib.AbstractExecutionAction
import it.unibo.jakta.actions.stdlib.Print
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import org.gciatto.kt.math.BigInteger
import java.util.*

class TestPlans : DescribeSpec({
    val strawberryDesire = ASBelief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
    val chocolateNeed = ASBelief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))
    val genericDesire = ASBelief.fromSelfSource(Struct.of("desire", Jakta.parseVar("X")))

    val plan = ASPlan.ofBeliefBaseAddition(genericDesire, emptyList())
    val planLibrary = listOf(plan)

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
            ap.apply(event).first() shouldBe action
        }

        it("should run if and only if the context is valid, and unify those values") {
            data class VerifySubstitution(var term: Term, val expected: Int): AbstractExecutionAction("VerifySubstitution",1) {
                override fun applySubstitution(substitution: Substitution) {
                    term = substitution.applyTo(term) ?: io.kotest.assertions.fail("Failed to apply substitution to $term")
                    println("substitution $substitution applied to $term")
                }

                override fun invoke(p1: ActionInvocationContext): List<SideEffect> {
                    println("The term is $term")
                    term.asInteger()?.value shouldBe BigInteger.of(expected)
                    return emptyList()
                }

            }

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
            ap.apply(event).first().invoke(ActionRequest.of(ASAgent.of().context, Time.real(Calendar.getInstance().timeInMillis)))
           // ap.apply(event).first() shouldBe Atom.of("0")
           // ap.apply(event)[1] shouldBe Atom.of("1")
        }
    }
})
