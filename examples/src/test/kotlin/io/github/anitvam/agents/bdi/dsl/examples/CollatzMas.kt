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
                execute("print"("Collatz Conjecture verified!"))
                execute("stop")
            }

            + achieve("collatz"(X)) onlyIf {
                (0 arithEq X % 2) and (R `is` (X intDiv 2))
            } then {
                spawn("verify"(R))
                + "explored"("source"("self"), X)
                execute("print"(R))
                achieve("collatz"(R))
            }

            + achieve("collatz"(X)) onlyIf {
                (1 arithEq X % 2) and (R `is` ((X * 3) + 1))
            } then {
                spawn("verify"(R))
                + "explored"("source"("self"), X)
                execute("print"(R))
                achieve("collatz"(R))
            }
        }
    }
}

fun main() {
    println("Starting Collatz Conjecture verification for 33...")
    collatzMas(33).start()
}
