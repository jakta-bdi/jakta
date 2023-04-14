package io.github.anitvam.agents.bdi.dsl

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
                fact { "good"("ice-cream") }
            }
            goals {
                achieve("eat"("ice-cream"))
            }
            plans {
                + achieve("eat"("ice-cream")) onlyIf { "good"("source"("self"), "ice-cream") } then {
                    execute("pluto"("YUMMY!"))
                }
            }
        }
    }.start()
}
