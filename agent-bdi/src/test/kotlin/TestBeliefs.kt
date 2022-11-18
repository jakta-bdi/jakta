import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.BeliefUpdate
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var

class TestBeliefs : DescribeSpec({
    val genericDesire = Belief.of(Struct.of("desire", Var.of("X")))
    val chocolateDesire = Belief.of(Struct.of("desire", Atom.of("chocolate")))
    val strawberryDesire = Belief.of(Struct.of("desire", Atom.of("strawberry")))
    val genericNeed = Belief.of(Struct.of("need", Var.of("X")))
    val emptybb = BeliefBase.empty()

    describe("A belief") {
        it("Should be added to a Belief Base") {
            emptybb.count() shouldBe 0
            val rr = emptybb.add(chocolateDesire)
            rr.modifiedBeliefs.size shouldBe 1
            rr.modifiedBeliefs.first().belief shouldBe chocolateDesire
            rr.modifiedBeliefs.first().updateType shouldBe BeliefUpdate.UpdateType.ADDITION
            val bb = rr.updatedBeliefBase
            bb.count() shouldBe 1
            bb.forEach { println(it) }
        }

        it("Should not be added to a Belief Base two times") {
            var bb = emptybb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb.forEach { println(it) }
        }

        it("Should be added to a BeliefBase with a Belief with the same predicate") {
            var bb = emptybb.add(strawberryDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 2
            bb.forEach { println(it) }
        }

        it("should not be added to a BeliefBase with the same predicate and a Variable") {
            var bb = emptybb.add(genericDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb = bb.add(chocolateDesire).updatedBeliefBase
            bb.count() shouldBe 1
            bb.forEach { println(it) }
        }

        it("should be removed from a BeliefBase") {
            var bb = BeliefBase.of(listOf(strawberryDesire, chocolateDesire, genericNeed))
            bb.count() shouldBe 3

            bb = bb.remove(genericNeed).updatedBeliefBase
            bb.count() shouldBe 2

            val rr = bb.remove(strawberryDesire)
            rr.modifiedBeliefs.size shouldBe 1
            rr.modifiedBeliefs.first().belief shouldBe strawberryDesire
            rr.modifiedBeliefs.first().updateType shouldBe BeliefUpdate.UpdateType.REMOVAL
            bb = rr.updatedBeliefBase
            bb.count() shouldBe 1
            bb.first() shouldBe chocolateDesire
        }
    }

    describe("A BeliefBase") {
        it("should be added into another one") {
            var bb = BeliefBase.of(listOf(strawberryDesire))
            bb.count() shouldBe 1

            val bb2 = BeliefBase.of(listOf(genericNeed, chocolateDesire))
            bb2.count() shouldBe 2

            val rr = bb.addAll(bb2)
            rr.modifiedBeliefs.size shouldBe 2
            rr.modifiedBeliefs.map { it.belief } shouldBe listOf(genericNeed, chocolateDesire)
            rr.modifiedBeliefs.map { it.updateType } shouldBe
                listOf(BeliefUpdate.UpdateType.ADDITION, BeliefUpdate.UpdateType.ADDITION)

            bb = rr.updatedBeliefBase
            bb.count() shouldBe 3
        }

        it("should be removed from another") {
            var bb = BeliefBase.of(listOf(genericNeed, chocolateDesire, strawberryDesire))
            bb.count() shouldBe 3

            val bb2 = BeliefBase.of(listOf(chocolateDesire, strawberryDesire))
            bb2.count() shouldBe 2

            val rr = bb.removeAll(bb2)
            rr.modifiedBeliefs.map { it.belief } shouldBe listOf(chocolateDesire, strawberryDesire)
            rr.modifiedBeliefs.map { it.updateType } shouldBe
                listOf(BeliefUpdate.UpdateType.REMOVAL, BeliefUpdate.UpdateType.REMOVAL)

            bb = rr.updatedBeliefBase
            bb.count() shouldBe 1
            bb.first() shouldBe genericNeed
        }

        it("should be solved") {
            BeliefBase.of(listOf(strawberryDesire))
                .solve(Struct.of("desire", Var.of("X")))
                .substitution.values.first() shouldBe Atom.of("strawberry")
            val substitution = BeliefBase.of(listOf(genericDesire))
                .solve(Struct.of("desire", Atom.of("strawberry"))).substitution
            substitution.isSuccess shouldBe true
            substitution.values.size shouldBe 0
        }
    }
})
