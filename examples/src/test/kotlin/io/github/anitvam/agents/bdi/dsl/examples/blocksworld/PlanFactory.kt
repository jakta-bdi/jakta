package io.github.anitvam.agents.bdi.dsl.examples.blocksworld

import io.github.anitvam.agents.bdi.dsl.plans

object PlanFactory {
    fun smartStrategy() = plans {
        +achieve("solve_with_strategy"("on"(X, Y), "on"(Y, Z), "on"(Z, T))) then {
            achieve("move"(Z, T))
            achieve("move"(Y, Z))
            achieve("move"(X, Y))
        }

        +achieve("move"(X, Y)) iff {
            "on"("source"("percept"), X, Y)
        }

        +achieve("move"(X, Y)) iff {
            "clear"("source"("self"), X) and "clear"("source"("self"), Y)
        } then {
            act("move"(X, Y))
        }

        +achieve("move"(X, Y)) iff {
            "clear"("source"("self"), X) and "on"("source"("percept"), W, Y)
        } then {
            achieve("move"(W, "table"))
            act("move"(X, Y))
        }

        +achieve("move"(X, Y)) iff {
            "on"("source"("percept"), W, X) and "clear"("source"("self"), Y)
        } then {
            achieve("move"(W, "table"))
            act("move"(X, Y))
        }

        +achieve("move"(X, Y)) iff {
            "on"("source"("percept"), W, X) and "on"("source"("percept"), V, Y)
        } then {
            achieve("move"(W, "table"))
            achieve("move"(V, "table"))
            act("move"(X, Y))
        }
    }

    fun dummyStrategy() = plans {
        +achieve("solve_with_strategy"("on"(X, Y), "on"(Y, Z), "on"(Z, T))) then {
            achieve("free"(X))
            achieve("free"(Y))
            achieve("free"(Z))
            act("move"(Z, T))
            act("move"(Y, Z))
            act("move"(X, Y))
        }

        +achieve("free"(X)) iff {
            "clear"("source"("self"), X)
        }

        +achieve("free"(X)) iff {
            "on"("source"("percept"), Y, X) and "clear"("source"("self"), Y)
        } then {
            act("move"(Y, "table"))
        }

        +achieve("free"(X)) iff {
            "on"("source"("percept"), Y, X) and not("clear"("source"("self"), Y))
        } then {
            achieve("free"(Y))
            achieve("free"(X))
        }
    }
}