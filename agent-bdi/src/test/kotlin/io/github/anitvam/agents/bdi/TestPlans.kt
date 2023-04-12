package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.theory.Theory

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

            val bb = BeliefBase.of(listOf(chocolateNeed))

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
    }
})

fun main() {

    val operatorExtension = Theory.of(
        Jakta.parseClause("&(A, B) :- A, B"),
        Jakta.parseClause("|(A, _) :- A"),
        Jakta.parseClause("|(_, B) :- B"),
        Jakta.parseClause("~(X) :- not(X)"),
    )
    val solver = Solver.prolog.newBuilder()
        .staticKb(operatorExtension)
        .build()
    println(solver.solveOnce(Struct.Companion.of("f", Integer.of(1))))
}
