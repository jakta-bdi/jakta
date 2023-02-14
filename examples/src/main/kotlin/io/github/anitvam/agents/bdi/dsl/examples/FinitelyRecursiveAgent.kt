package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.dsl.mas

fun main() {
    mas {
        agent("alice") {
            beliefs {
                fact("run"(X))
                rule("fun"(X) impliedBy Y)
            }
            goals {
                achieve("start")
            }
            plans {
                + achieve("start"(N, M)) then {
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
