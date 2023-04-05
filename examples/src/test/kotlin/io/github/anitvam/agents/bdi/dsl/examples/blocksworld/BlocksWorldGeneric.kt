package io.github.anitvam.agents.bdi.dsl.examples.blocksworld

import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.dsl.plans
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun getPlans(): Iterable<Plan> = plans {
    + achieve("move"(X, Y)) iff {
        "on"("source"("percept"), X, Y)
    }
    + achieve("move"(X, Y)) iff {
        "clear"("source"("self"), X) and "clear"("source"("self"), Y)
    } then {
        act("move"(X, Y))
    }
    + achieve("move"(X, Y)) iff {
        "clear"("source"("self"), X) and "on"("source"("percept"), W, Y)
    } then {
        achieve("move"(W, "table"))
        act("move"(X, Y))
    }
    + achieve("move"(X, Y)) iff {
        "on"("source"("percept"), W, X) and "clear"("source"("self"), Y)
    } then {
        achieve("move"(W, "table"))
        act("move"(X, Y))
    }
    + achieve("move"(X, Y)) iff {
        "on"("source"("percept"), W, X) and "on"("source"("percept"), V, Y)
    } then {
        achieve("move"(W, "table"))
        achieve("move"(V, "table"))
        act("move"(X, Y))
    }
}

fun getPlanBody(solutionGoal: Struct): List<Goal> {
    var list = listOf<Goal>()
    // stack from the bottom of the pile
    for (s in solutionGoal.args.reversed()) {
        list = list + Achieve.of(Struct.of("move", s.castToStruct().args))
    }
    return list
}

fun main() {
    val goal: Struct = Struct.of(
        "solve",
        Struct.of("on", Atom.of("y"), Atom.of("x")),
        Struct.of("on", Atom.of("x"), Atom.of("a")),
        Struct.of("on", Atom.of("a"), Atom.of("b")),
        Struct.of("on", Atom.of("b"), Atom.of("c")),
        Struct.of("on", Atom.of("c"), Atom.of("table")),
    )
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
                achieve(goal)
            }
            plans(getPlans())
            plans {
                + achieve(goal) then {
                    from(getPlanBody(goal))
                    iact("print"("Done!"))
                }
            }
        }
    }.start()
}
