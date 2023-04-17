package it.unibo.jakta.agents.bdi.dsl.examples.blocksworld

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

object PlanBodyFactory {
    fun smartStrategy(struct: Struct): List<Goal> {
        var list = listOf<Goal>()
        // stack from the bottom of the pile
        for (s in struct.args.reversed()) {
            list = list + Achieve.of(Struct.of("move", s.castToStruct().args))
        }
        return list
    }

    fun dummyStrategy(struct: Struct): List<Goal> {
        var list = listOf<Goal>()
        // clear all blocks
        for (s in struct.args) {
            list = list + Achieve.of(Struct.of("move", s.castToStruct().args.first(), Atom.of("table")))
        }
        // stack blocks
        for (s in struct.args.reversed()) {
            list = list + Achieve.of(Struct.of("move", s.castToStruct().args))
        }
        return list
    }
}

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

            // smartStrategy("on"("A", "B"), "on"("B", "C"), "on"("C", "table"))
            goals {
                achieve("solve"("on"("a", "b"), "on"("b", "c"), "on"("c", "table")))
            }
            plans {
                + achieve("solve"("on"(X, Y), "on"(Y, Z), "on"(Z, T))) then {
                    from(PlanBodyFactory.smartStrategy("solve"("on"(X, Y), "on"(Y, Z), "on"(Z, T))))
                    // from(PlanBodyFactory.dummyStrategy("solve"("on"(X, Y), "on"(Y, Z), "on"(Z, T))))
                    iact("print"("End!"))
                }

                + achieve("move"(X, Y)) onlyIf {
                    "on"("source"("percept"), X, Y)
                }

                + achieve("move"(X, Y)) onlyIf {
                    "clear"("source"("self"), X) and "clear"("source"("self"), Y)
                } then {
                    execute("move"(X, Y))
                }

                + achieve("move"(X, Y)) onlyIf {
                    "clear"("source"("self"), X) and "on"("source"("percept"), W, Y)
                } then {
                    achieve("move"(W, "table"))
                    execute("move"(X, Y))
                }

                + achieve("move"(X, Y)) onlyIf {
                    "on"("source"("percept"), W, X) and "clear"("source"("self"), Y)
                } then {
                    achieve("move"(W, "table"))
                    execute("move"(X, Y))
                }

                + achieve("move"(X, Y)) onlyIf {
                    "on"("source"("percept"), W, X) and "on"("source"("percept"), V, Y)
                } then {
                    achieve("move"(W, "table"))
                    achieve("move"(V, "table"))
                    execute("move"(X, Y))
                }
            }
        }
    }.start()
}
