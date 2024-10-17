package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.dsl.beliefs.BeliefsScope
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestBeliefsDsl : DescribeSpec({
    fun testingBeliefs(function: BeliefsScope.() -> Unit) = BeliefsScope().also { it.function() }.build()

    describe("An agent beliefs") {
        it("can be described through functions") {
            val bb = testingBeliefs {
                fact { "turn"("me") }
                fact { "other"("ponger") }
                rule { "s"("nat"(X)) impliedBy "s"(X) }
            }
            bb.count() shouldBe 3
        }
        it("can be described through methods") {
            val bb = testingBeliefs {
                fact("turn"("me"))
                fact("other"("ponger"))
                rule("s"("nat"(X)) impliedBy "s"(X))
            }
            bb.count() shouldBe 3
        }
    }
    describe("Agents Facts") {
        it("can be declared using LogicProgrammingScope function as Atoms") {
            val bb = testingBeliefs {
                fact { "pippo" }
            }
            bb shouldContain Belief.fromSelfSource(Atom.of("pippo"))
            bb shouldContain Belief.fromSelfSource(Struct.of("pippo"))
        }

        it("can be declared passing Atoms as arguments") {
            val bb = testingBeliefs {
                fact("pippo")
            }
            bb shouldContain Belief.fromSelfSource(Atom.of("pippo"))
            bb shouldContain Belief.fromSelfSource(Struct.of("pippo"))
        }

        it("can be equivalently defined using explicit source or not") {
            val bbWithExplicitSource = testingBeliefs {
                fact("pippo".fromSelf)
            }
            val bbWithoutExplicitSource = testingBeliefs {
                fact("pippo")
            }
            bbWithoutExplicitSource shouldBe bbWithExplicitSource
        }

        it("can be equivalently defined using explicit source or not in LogicProgrammingScope") {
            val bbWithExplicitSource = testingBeliefs {
                fact {
                    "pippo".fromSelf
                }
            }
            val bbWithoutExplicitSource = testingBeliefs {
                fact {
                    "pippo"
                }
            }
            bbWithoutExplicitSource shouldBe bbWithExplicitSource
        }
    }
})
