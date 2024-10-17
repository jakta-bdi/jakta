package it.unibo.jakta

import it.unibo.jakta.dsl.AgentScope
import it.unibo.jakta.dsl.invoke
import it.unibo.tuprolog.core.Struct

fun AgentScope.planExtension(vararg goals: Struct) {
    goals {
        achieve("solve"(*goals))
    }
    plans {
        +achieve("solve"(X)) then {
            for (goal in goals) {
                achieve("move".invoke(goal.args))
            }
            execute("finished")
        }
    }
}
