package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.dsl.mas

fun collatzMas(number: Int) = mas {
    agent("collatz-agent") {
        goals {
            achieve("collatz"(number))
        }
        plans {
            + achieve("verify"(X)) onlyIf {
                (X arithEq 4) and "explored"("source"("self"), 4)
            } then {
                iact("print"("Collatz Conjecture verified!"))
                iact("stop")
            }

            + achieve("collatz"(X)) onlyIf {
                (0 arithEq X % 2) and (R `is` (X intDiv 2))
            } then {
                spawn("verify"(R))
                + "explored"("source"("self"), X)
                iact("print"(R))
                achieve("collatz"(R))
            }

            + achieve("collatz"(X)) onlyIf {
                (1 arithEq X % 2) and (R `is` ((X * 3) + 1))
            } then {
                spawn("verify"(R))
                + "explored"("source"("self"), X)
                iact("print"(R))
                achieve("collatz"(R))
            }
        }
    }
}

fun main() {
    collatzMas(10).start()
}
