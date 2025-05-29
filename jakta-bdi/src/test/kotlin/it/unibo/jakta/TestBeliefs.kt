package it.unibo.jakta

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var

class TestBeliefs :
    DescribeSpec({
        val genericDesire = ASBelief.fromSelfSource(Struct.of("desire", Var.of("X")))
        val chocolateDesire = ASBelief.fromSelfSource(Struct.of("desire", Atom.of("chocolate")))
        val strawberryDesire = ASBelief.fromSelfSource(Struct.of("desire", Atom.of("strawberry")))
        val genericNeed = ASBelief.fromSelfSource(Struct.of("need", Var.of("X")))
        lateinit var emptybb: MutableBeliefBase
        beforeEach {
            emptybb = MutableBeliefBase.empty()
        }

        describe("A belief with self source") {
            it("Should be added to a Belief Base") {
                emptybb.count() shouldBe 0
                emptybb.add(chocolateDesire) shouldBe true
                // emptybb.events.size shouldBe 1
                emptybb.poll().shouldBeInstanceOf<BeliefBaseAddition>()
                emptybb.isEmpty() shouldBe false
                emptybb.count() shouldBe 1
                emptybb.size shouldBe 1
            }

            it("Should not be added to a Belief Base two times") {
                emptybb.add(chocolateDesire)
                emptybb.count() shouldBe 1
                emptybb.add(chocolateDesire)
                emptybb.count() shouldBe 1
            }

            it("Should be added to a ASBeliefBase with a Belief with the same predicate") {
                emptybb.add(strawberryDesire)
                emptybb.count() shouldBe 1
                emptybb.add(chocolateDesire)
                emptybb.count() shouldBe 2
            }

            it("should not be added to a ASBeliefBase with the same predicate and a Variable") {
                emptybb.add(genericDesire)
                emptybb.count() shouldBe 1
                emptybb.add(chocolateDesire)
                emptybb.count() shouldBe 1
            }

            it("should be removed from a ASBeliefBase") {
                val bb = MutableBeliefBase.of(listOf(strawberryDesire, chocolateDesire, genericNeed))
                bb.count() shouldBe 3

                bb.remove(genericNeed)
                bb.count() shouldBe 2
                bb.remove(strawberryDesire)
                // bb.events.size shouldBe 2
                bb.poll().shouldBeInstanceOf<BeliefBaseRemoval>()
                bb.poll().shouldBeInstanceOf<BeliefBaseRemoval>()

                bb.count() shouldBe 1
                bb.first() shouldBe chocolateDesire
            }
        }

        describe("A ASBeliefBase") {
            it("should be added into another one") {
                val bb = MutableBeliefBase.of(listOf(strawberryDesire))
                bb.count() shouldBe 1

                val bb2 = MutableBeliefBase.of(listOf(genericNeed, chocolateDesire))
                bb2.count() shouldBe 2

                bb.addAll(bb2)
                // bb.events.size shouldBe 2
                val e1 = bb.poll().shouldBeInstanceOf<BeliefBaseAddition>()
                val e2 = bb.poll().shouldBeInstanceOf<BeliefBaseAddition>()
                listOf(e1, e2).map {
                    ASBelief.from(it.value)
                } shouldBe listOf(genericNeed, chocolateDesire)
                bb.count() shouldBe 3
            }

            it("should be removed from another") {
                val bb = MutableBeliefBase.of(listOf(genericNeed, chocolateDesire, strawberryDesire))
                bb.count() shouldBe 3

                val bb2 = MutableBeliefBase.of(listOf(chocolateDesire, strawberryDesire))
                bb2.count() shouldBe 2

                bb.removeAll(bb2)
                // bb.events.count() shouldBe 2
                val e1 = bb.poll().shouldBeInstanceOf<BeliefBaseRemoval>()
                val e2 = bb.poll().shouldBeInstanceOf<BeliefBaseRemoval>()
                listOf(e1, e2).map {
                    ASBelief.from(it.value)
                } shouldBe listOf(chocolateDesire, strawberryDesire)

                bb.count() shouldBe 1
                bb.first() shouldBe genericNeed
            }

            it("should be solved") {
                var substitution =
                    MutableBeliefBase
                        .of(listOf(strawberryDesire))
                        .also {
                            it.isEmpty() shouldBe false
                            it.size shouldBe 1
                        }.select(genericDesire.content.head)
                        .substitution
                substitution.isFailed shouldBe false
                substitution.values.first() shouldBe Atom.of("strawberry")
                substitution =
                    MutableBeliefBase
                        .of(listOf(genericDesire))
                        .select(strawberryDesire.content.head)
                        .substitution
                substitution.isSuccess shouldBe true
                substitution.values.size shouldBe 0
            }
        }

        describe("the belief update function") {
            it("should not remove beliefs with source(self)") {
                val belief = ASBelief.fromSelfSource(Struct.of("something", Var.of("X")))
                val bb = MutableBeliefBase.of()
                bb.update(belief)
                // bb.events.size shouldBe 0
                bb.poll() shouldBe null
                bb.count() shouldBe 0
            }
        }

        describe("A belief annotation") {
            it("should be solved as well") {
                val bb = MutableBeliefBase.of(ASBelief.fromSelfSource(Struct.of("coffee", Atom.of("hot"))))
                bb
                    .select(
                        Struct.of("coffee", Struct.of("source", Var.of("X")), Atom.of("hot")),
                    ).substitution.values
                    .first() shouldBe Atom.of("self")
            }
        }
    })
