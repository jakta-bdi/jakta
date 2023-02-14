package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.dsl.mas

fun main() {
    mas {
        agent("alice") {
            goals {
                achieve("start"(0, 10))
            }
            plans {
                + achieve("start"(N, N)) then {
                    iact("print"("Hello World!", N))
                }
                + achieve("start"(N, M)) iff { (N lowerThan M) and (S `is` (N + 1)) } then {
                    iact("print"("Hello World!", N))
                    achieve("start"(S, M))
                }
            }
        }
    }.start()
}
