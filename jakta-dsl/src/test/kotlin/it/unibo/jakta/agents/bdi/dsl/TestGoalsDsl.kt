package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec

class TestGoalsDsl :
    DescribeSpec({
        describe("Agent's initial goals") {
            it("can be specified using the DSL") {
                val goals =
                    mas {
                        agent("test") {
                            goals {
                                achieve("send_ping"(E))
                                achieve("send_ping")
                                test("sendMessageTo"("ball", R))
                            }
                        }
                    }
                println(goals)
            }
        }
    })
