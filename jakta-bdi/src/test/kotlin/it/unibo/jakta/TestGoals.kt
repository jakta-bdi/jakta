package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.jakta.actions.stdlib.Achieve
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var

class TestGoals : DescribeSpec({
    val strawberryDesire = ASBelief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
    val belief = ASBelief.fromSelfSource(Struct.of("desire", Var.of("X")))

    describe("A PrologGoal") {
        it("should apply a substitution to its variables") {
            val substitution = ASMutableBeliefBase.of(listOf(strawberryDesire))
                .select(belief.content.head)
                .substitution

            val goal = Achieve(belief.content)
            goal.planTrigger shouldBe belief.content

            val substitutedGoal = goal.applySubstitution(substitution)
            goal.planTrigger shouldBe belief.content
            substitutedGoal.shouldBeInstanceOf<Achieve>()
            substitutedGoal.planTrigger shouldBe strawberryDesire.content
        }
        it("should perform copy and keep the actual PrologGoal type") {
//            val goal = Print(strawberryDesire.content)
//            goal.signature.name shouldBe "Print"
//            when (val goalCopy = goal.copy(belief.rule)) {
//                is Print -> goalCopy.value shouldBe belief.content
//                else -> fail("Copy doesn't keep the PrologGoal type")
//            } TODO("Copy needs to be added again?")
        }
    }
})
