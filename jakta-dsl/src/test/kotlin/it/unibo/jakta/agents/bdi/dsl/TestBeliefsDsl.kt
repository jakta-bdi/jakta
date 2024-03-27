package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

class TestBeliefsDsl : DescribeSpec({
    describe("An agent beliefs") {
        it("can be described through functions") {
            val mas = mas {
                agent("test") {
                    beliefs {
                        fact { "turn"("me") }
                        fact { "other"("ponger") }
                        rule { "s"("nat"(X)) impliedBy "s"(X) }
                    }
                }
            }
            val bb = mas.agents.first().context.beliefBase
            bb.count() shouldBe 3
        }
        it("can be described through methods") {
            val mas = mas {
                agent("test") {
                    beliefs {
                        fact("turn"("me"))
                        fact("other"("ponger"))
                        rule("s"("nat"(X)) impliedBy "s"(X))
                    }
                }
            }
            val bb = mas.agents.first().context.beliefBase
            bb.count() shouldBe 3
        }
    }
    describe("Agents Facts") {
        it("can be declared using LogicProgrammingScope function as Atoms") {
            val mas = mas {
                agent("fact with fun") {
                    beliefs {
                        fact { "pippo" }
                    }
                }
            }
            val bb = mas.agents.first().context.beliefBase
            bb shouldContain Belief.fromSelfSource(Atom.of("pippo"))
            bb shouldContain Belief.fromSelfSource(Struct.of("pippo"))
        }

        it("can be declared passing Atoms as arguments") {
            val mas = mas {
                agent("fact with args") {
                    beliefs {
                        fact("pippo")
                    }
                }
            }
            val bb = mas.agents.first().context.beliefBase
            bb shouldContain Belief.fromSelfSource(Atom.of("pippo"))
            bb shouldContain Belief.fromSelfSource(Struct.of("pippo"))
        }
    }
})
