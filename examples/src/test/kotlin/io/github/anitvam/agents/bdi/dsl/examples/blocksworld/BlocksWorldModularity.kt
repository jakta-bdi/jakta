package io.github.anitvam.agents.bdi.dsl.examples.blocksworld

import io.github.anitvam.agents.bdi.dsl.environment
import io.github.anitvam.agents.bdi.dsl.mas
import it.unibo.tuprolog.core.Atom

fun main() {
    mas {
        environment {
            from(BlocksWorldEnvironment())
            actions {
                action("move", 2) {
                    val upper: Atom = argument(0)
                    val lower: Atom = argument(1)
                    this.updateData(mapOf("upper" to upper, "lower" to lower))
                }
            }
        }
        agent("block-agent") {
            beliefs {
                rule("clear"(X) impliedBy (not("on"("source"("percept"), `_`, X)) or "table"))
            }

            goals {
                achieve("solve"("on"("a", "b"), "on"("b", "c"), "on"("c", "table")))
            }
            plans(PlanFactory.dummyStrategy())
            plans {
                + achieve("solve"(A, B, C)) then {
                    achieve("solve_with_strategy"(A, B, C))
                    iact("print"("End!"))
                }
            }
        }
    }.start()
}
