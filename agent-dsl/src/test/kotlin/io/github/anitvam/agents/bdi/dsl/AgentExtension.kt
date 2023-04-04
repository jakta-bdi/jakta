package io.github.anitvam.agents.bdi.dsl

import it.unibo.tuprolog.core.Struct

fun AgentScope.planExtension(vararg goals: Struct) {
    goals {
        achieve("solve"(*goals))
    }
    plans {
        + achieve("solve"(X)) then {
            for (goal in goals) {
                achieve("move".invoke(goal.args))
            }
            act("finished")
        }
    }
}
