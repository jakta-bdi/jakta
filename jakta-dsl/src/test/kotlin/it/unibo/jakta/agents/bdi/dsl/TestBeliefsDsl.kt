package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

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
    }
})
