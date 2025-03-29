package it.unibo.jakta.agents.bdi

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TestAgentBeliefBaseRevision :
    DescribeSpec({
        describe("An empty agent") {
            it("should have empty sets in its Context") {
                val agentContext = Agent.empty().context
                agentContext.beliefBase.count() shouldBe 0
                agentContext.events.size shouldBe 0
                agentContext.intentions.size shouldBe 0
                agentContext.planLibrary.plans.size shouldBe 0
            }
        }

        describe("An agent") {
            it("should print") {
            }
        }
    })
