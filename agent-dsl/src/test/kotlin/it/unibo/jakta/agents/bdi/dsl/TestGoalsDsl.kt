package it.unibo.jakta.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec

class TestGoalsDsl : DescribeSpec({
    describe("Agent's initial goals") {
        it("can be specified using the DSL") {
            val goals = mas {
                agent("test") {
                    goals {
                        achieve("send_ping"(E)) // Atoms are not converted to Structs from LogicProgrammingScope ???
                        achieve("send_ping") // Atoms are not converted to Structs from LogicProgrammingScope ???
                        test("sendMessageTo"("ball", R))
                    }
                }
            }
            println(goals)
        }
    }
})
