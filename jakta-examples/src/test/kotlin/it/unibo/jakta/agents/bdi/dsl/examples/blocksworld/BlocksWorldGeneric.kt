package it.unibo.jakta.agents.bdi.dsl.examples.blocksworld

import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.dsl.plans
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.lp
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.on
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.source
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.percept
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.self
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.move
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.solve
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.clear
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.a
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.b
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.c
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.x
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.y
import it.unibo.jakta.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.table


fun getPlans(): Iterable<Plan> = plans {
    + achieve(move(X, Y)) onlyIf {
        on(source(percept), X, Y)
    }
    + achieve(move(X, Y)) onlyIf {
        clear(source(self), X) and clear(source(self), Y)
    } then {
        execute(move(X, Y))
    }
    + achieve(move(X, Y)) onlyIf {
        clear(source(self), X) and on(source(percept), W, Y)
    } then {
        achieve(move(W, "table"))
        execute(move(X, Y))
    }
    + achieve(move(X, Y)) onlyIf {
        on(source(percept), W, X) and clear(source(self), Y)
    } then {
        achieve(move(W, table))
        execute(move(X, Y))
    }
    + achieve("move"(X, Y)) onlyIf {
        "on"("source"("percept"), W, X) and "on"("source"("percept"), V, Y)
    } then {
        achieve("move"(W, "table"))
        achieve("move"(V, "table"))
        execute("move"(X, Y))
    }
}

fun getPlanBody(solutionGoal: Struct): List<Goal> = solutionGoal.args.reversed().map { solution ->
    Achieve.of(Struct.of("move", solution.castToStruct().args))
}

fun main() {
    val goal: Struct = lp {
        solve(on(y, x), on(x, a), on(a, b), on(b, c), on(c, table))
    }
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
