package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.context.ContextUpdate
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var

class TestBeliefs : DescribeSpec({
    val genericDesire = Belief.fromSelfSource(Struct.of("desire", Var.of("X")))
    val chocolateDesire = Belief.fromSelfSource(Struct.of("desire", Atom.of("chocolate")))
    val strawberryDesire = Belief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
    val genericNeed = Belief.fromSelfSource(Struct.of("need", Var.of("X")))
    val emptybb = PrologBeliefBase.empty()

    describe("A belief with self source") {
        it("Should be added to a Belief Base") {
            emptybb.count() shouldBe 0
            val rr = emptybb.add(chocolateDesire)
            rr.modifiedBeliefs.size shouldBe 1
            rr.modifiedBeliefs.first().belief shouldBe chocolateDesire
            rr.modifiedBeliefs.first().updateType shouldBe ContextUpdate.ADDITION
            println(rr.modifiedBeliefs)
            println(rr.updatedBeliefBase)
            val bb = rr.updatedBeliefBase
            println(bb)
            println(bb.count())
            bb.count() shouldBe 1
        }

        it("Should not be added to a Belief Base two times") {
            var bb = emptybb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
        }

        it("Should be added to a PrologBeliefBase with a Belief with the same predicate") {
            var bb = emptybb.add(strawberryDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 2
        }

        it("should not be added to a PrologBeliefBase with the same predicate and a Variable") {
            var bb = emptybb.add(genericDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
        }

        it("should be removed from a PrologBeliefBase") {
            var bb = PrologBeliefBase.of(listOf(strawberryDesire, chocolateDesire, genericNeed))
            bb.count() shouldBe 3

            bb = bb.remove(genericNeed).updatedBeliefBase
            bb.count() shouldBe 2

            val rr = bb.remove(strawberryDesire)
            rr.modifiedBeliefs.size shouldBe 1
            rr.modifiedBeliefs.first().belief shouldBe strawberryDesire
            rr.modifiedBeliefs.first().updateType shouldBe ContextUpdate.REMOVAL
            bb = rr.updatedBeliefBase
            bb.count() shouldBe 1
            bb.first() shouldBe chocolateDesire
        }
    }

    describe("A PrologBeliefBase") {
        it("should be added into another one") {
            var bb = PrologBeliefBase.of(listOf(strawberryDesire))
            bb.count() shouldBe 1

            val bb2 = PrologBeliefBase.of(listOf(genericNeed, chocolateDesire))
            bb2.count() shouldBe 2

            val rr = bb.addAll(bb2)
            rr.modifiedBeliefs.size shouldBe 2
            rr.modifiedBeliefs.map { it.belief } shouldBe listOf(genericNeed, chocolateDesire)
            rr.modifiedBeliefs.map { it.updateType } shouldBe
                listOf(ContextUpdate.ADDITION, ContextUpdate.ADDITION)

            bb = rr.updatedBeliefBase
            bb.count() shouldBe 3
        }

        it("should be removed from another") {
            var bb = PrologBeliefBase.of(listOf(genericNeed, chocolateDesire, strawberryDesire))
            bb.count() shouldBe 3

            val bb2 = PrologBeliefBase.of(listOf(chocolateDesire, strawberryDesire))
            bb2.count() shouldBe 2

            val rr = bb.removeAll(bb2)
            rr.modifiedBeliefs.map { it.belief } shouldBe listOf(chocolateDesire, strawberryDesire)
            rr.modifiedBeliefs.map { it.updateType } shouldBe
                listOf(ContextUpdate.REMOVAL, ContextUpdate.REMOVAL)

            bb = rr.updatedBeliefBase
            bb.count() shouldBe 1
            bb.first() shouldBe genericNeed
        }

        it("should be solved") {

            var substitution = PrologBeliefBase.of(listOf(strawberryDesire))
                .solve(genericDesire).substitution
            substitution.isFailed shouldBe false
            substitution.values.first() shouldBe Atom.of("strawberry")
            substitution = PrologBeliefBase.of(listOf(genericDesire))
                .solve(strawberryDesire).substitution
            substitution.isSuccess shouldBe true
            substitution.values.size shouldBe 0
        }
    }

    describe("the belief update function") {
        it("should not remove beliefs with source(self)") {
            val belief = Belief.fromSelfSource(Struct.of("something", Var.of("X")))
            val agent = Agent.of(beliefBase = PrologBeliefBase.of(listOf(belief)))
            val al = AgentLifecycle.newLifecycleFor(agent)
            val rr = al.updateBelief(PrologBeliefBase.empty(), agent.context.beliefBase)
            println(agent.context.beliefBase)
            println(rr.modifiedBeliefs)
            println(rr.modifiedBeliefs.count())
            rr.modifiedBeliefs.size shouldBe 0
            rr.updatedBeliefBase.count() shouldBe 1
        }
    }

    describe("A belief annotation") {
        it("should be solved as well") {
            val bb = PrologBeliefBase.of(Belief.fromSelfSource(Struct.of("coffee", Atom.of("hot"))))
            bb.solve(
                Struct.of("coffee", Struct.of("source", Var.of("X")), Atom.of("hot")),
            ).substitution.values.first() shouldBe Atom.of("self")
        }
    }
})
