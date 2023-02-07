package io.github.anitvam.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec

class TestBeliefsDsl : DescribeSpec({
    describe("...") {
        it("...") {
            mas {
                agent("test") {
                    beliefs {
                        fact { "turn"("me") }
                        fact { "other"("ponger") }
                        rule { "s"("nat"(X)) impliedBy "s"(X) }
                    }
                }
            }
        }
    }
})
