package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.events.Event
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.ActInternally
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var

class TestPlans : DescribeSpec({
    val strawberryDesire = Belief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
    val chocolateNeed = Belief.fromSelfSource(Struct.of("need", Atom.of("chocolate")))
    val genericDesire = Belief.fromSelfSource(Struct.of("desire", Var.of("X")))

    val plan = Plan.ofBeliefBaseAddition(genericDesire, emptyList())
    val planLibrary = PlanLibrary.of(listOf(plan))

    describe("A Plan") {
        it("should be relevant if its trigger is unified with the event value") {
            val event = Event.ofBeliefBaseAddition(strawberryDesire)
            planLibrary.plans.size shouldBe 1
            planLibrary.plans.first() shouldBe plan

            val relevantPlans = planLibrary.relevantPlans(event)

            relevantPlans.plans.size shouldBe 1
            relevantPlans.plans.first() shouldBe plan

            val planLibrary2 = PlanLibrary.of(listOf(Plan.ofBeliefBaseRemoval(genericDesire, emptyList())))
            planLibrary2.relevantPlans(event).plans.size shouldBe 0
        }

        it("should be applicable if its trigger is a valid Predicate") {
            val event = Event.ofBeliefBaseAddition(strawberryDesire)

            val bb = ASBeliefBase.of(listOf(chocolateNeed))

            plan.isApplicable(event, bb) shouldBe true

            val plan2 = Plan.ofBeliefBaseAddition(
                belief = genericDesire,
                guard = Truth.FALSE,
                goals = emptyList(),
            )
            plan2.isApplicable(event, bb) shouldBe false

            val plan3 = Plan.ofBeliefBaseRemoval(genericDesire, emptyList())
            plan3.isApplicable(event, bb) shouldBe false
        }

        it("should unify the triggering event variables") {
            val event = Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("start(0, 10)")))
            val p = Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("start(S, M)"),
                goals = listOf(ActInternally.of(Jakta.parseStruct("print(S)"))),
            )
            p.isApplicable(event, ASBeliefBase.of()) shouldBe true
            val ap = p.applicablePlan(event, ASBeliefBase.of())
            ap.trigger.value shouldBe event.trigger.value
            ap.goals.first().value shouldBe Jakta.parseStruct("print(0)")
        }

        it("should run if and only if the context is valid, and unify those values") {
            val event = Event.ofAchievementGoalInvocation(Achieve.of(Jakta.parseStruct("pippo(0)")))
            val p = Plan.ofAchievementGoalInvocation(
                value = Jakta.parseStruct("pippo(S)"),
                guard = Jakta.parseStruct("S < 5 & N is S + 1"),
                goals = listOf(
                    ActInternally.of(Jakta.parseStruct("print(S)")),
                    ActInternally.of(Jakta.parseStruct("print(N)")),
                ),
            )
            val ap = p.applicablePlan(event, ASBeliefBase.of())
            ap.goals.first().value shouldBe Jakta.parseStruct("print(0)")
            ap.goals[1].value shouldBe Jakta.parseStruct("print(1)")
        }
    }
})
