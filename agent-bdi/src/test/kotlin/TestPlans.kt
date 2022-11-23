import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var

class TestPlans : DescribeSpec({
    val strawberryDesire = Belief.of(Struct.of("desire", Atom.of("strawberry")))
    val chocolateNeed = Belief.of(Struct.of("need", Atom.of("chocolate")))
    val genericDesire = Belief.of(Struct.of("desire", Var.of("X")))

    val plan = Plan.ofBeliefBaseAddition(genericDesire)
    val planLibrary = PlanLibrary.of(listOf(plan))

    describe("A Plan") {
        it("should be relevant if its trigger is unified with the event value") {
            val event = Event.ofBeliefBaseAddition(strawberryDesire)
            planLibrary.plans.size shouldBe 1
            planLibrary.plans.first() shouldBe plan

            val relevantPlans = planLibrary.relevantPlans(event)

            relevantPlans.plans.size shouldBe 1
            relevantPlans.plans.first() shouldBe plan

            val planLibrary2 = PlanLibrary.of(listOf(Plan.ofBeliefBaseRemoval(genericDesire)))
            planLibrary2.relevantPlans(event).plans.size shouldBe 0
        }

        it("should be applicable if its trigger is a valid Predicate") {
            val event = Event.ofBeliefBaseAddition(strawberryDesire)

            val bb = BeliefBase.of(listOf(chocolateNeed))

            plan.isApplicable(event, bb) shouldBe true

            val plan2 = Plan.ofBeliefBaseAddition(
                belief = genericDesire,
                guard = Truth.FALSE,
            )
            plan2.isApplicable(event, bb) shouldBe false

            val plan3 = Plan.ofBeliefBaseRemoval(genericDesire)
            plan3.isApplicable(event, bb) shouldBe false
        }
    }
})
