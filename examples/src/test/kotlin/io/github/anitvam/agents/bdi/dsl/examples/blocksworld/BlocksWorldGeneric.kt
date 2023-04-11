package io.github.anitvam.agents.bdi.dsl.examples.blocksworld

import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.dsl.plans
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.lp
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.on
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.source
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.percept
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.self
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.move
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.solve
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.clear
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.a
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.b
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.c
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.x
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.y
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.z
import io.github.anitvam.agents.bdi.dsl.examples.blocksworld.BlocksWorldLiterals.table


fun getPlans(): Iterable<Plan> = plans {
    + achieve(move(X, Y)) onlyIf {
        on(source(percept), X, Y)
    }
    + achieve(move(X, Y)) onlyIf {
        clear(source(self), X) and clear(source(self), Y)
    } then {
        act(move(X, Y))
    }
    + achieve(move(X, Y)) onlyIf {
        clear(source(self), X) and on(source(percept), W, Y)
    } then {
        achieve(move(W, "table"))
        act(move(X, Y))
    }
    + achieve(move(X, Y)) onlyIf {
        on(source(percept), W, X) and clear(source(self), Y)
    } then {
        achieve(move(W, table))
        act(move(X, Y))
    }
    + achieve("move"(X, Y)) onlyIf {
        "on"("source"("percept"), W, X) and "on"("source"("percept"), V, Y)
    } then {
        achieve("move"(W, "table"))
        achieve("move"(V, "table"))
        act("move"(X, Y))
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
