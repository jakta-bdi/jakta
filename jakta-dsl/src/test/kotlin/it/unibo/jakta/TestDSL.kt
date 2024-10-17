package it.unibo.jakta

import it.unibo.jakta.dsl.mas
import it.unibo.tuprolog.core.Struct

fun main() {
    mas {
        environment {
            actions {
                action("pluto", 1) {
                    val parameter: Struct = argument(0)
                    println(parameter)
                }
            }
        }
        agent("pippo") {
            beliefs {
                val first = "cell"(A, B, C)
                val second = "cell"(X, Y, Z)
                rule { first.fromSelf impliedBy second }
                fact { "good"("ice-cream").fromSelf }
            }
            goals {
                achieve("eat"("ice-cream"))
            }
            plans {
                +achieve("eat"("ice-cream")) onlyIf { "good"("ice-cream").fromSelf } then {
                    execute("pluto"("YUMMY!"))
                }
            }
        }
    }.start(true)
}
