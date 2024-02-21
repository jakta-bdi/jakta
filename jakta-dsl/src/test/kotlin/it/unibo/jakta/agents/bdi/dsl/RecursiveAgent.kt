package it.unibo.jakta.agents.bdi.dsl

fun main() {
    mas {
        agent("recursive agent") {
            goals {
                achieve("start"(0, 120))
            }
            plans {
                +achieve("start"(N, N)) then {
                    execute("print"(N))
                }

                +achieve("start"(N, M)) onlyIf { (N lowerThan M) and (S `is` (N + 1)) } then {
                    execute("print"("value", N))
                    achieve("start"(S, M))
                }
            }
        }
    }.start()
}
