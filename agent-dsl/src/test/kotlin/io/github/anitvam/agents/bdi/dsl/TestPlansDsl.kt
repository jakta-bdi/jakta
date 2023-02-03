package io.github.anitvam.agents.bdi.dsl

import io.kotest.core.spec.style.DescribeSpec

class TestPlansDsl : DescribeSpec({
    describe("A plan") {
        it("should be created using the dsl") {
            plans {
                achieve("send_ping") iff { "turn"("me") and "other"(R) } then {
                    achieve("sendMessageTo"("ball", R))
                    test("sent"("ping"))
                    spawn("sendMessageTo"("ball", R))
                    act("send"("ping"))
                    iact("send"("ping"))
                    add("sent"("ping"))
                    remove("sent"("ping"))
                    update("turn"("other"))
                }
            }

            val p1 = plans {
                achieve("send_ping") then {
                    achieve("sendMessageTo"("ball", R))
                }
            }
            println(p1)
            println()
            val p2 = plans {
                achieve("send_ping") iff { "turn"("me") and "other"(R) } then {
                    achieve("sendMessageTo"("ball", R))
                }
            }
            println(p2)
            println()
        }
    }
})
