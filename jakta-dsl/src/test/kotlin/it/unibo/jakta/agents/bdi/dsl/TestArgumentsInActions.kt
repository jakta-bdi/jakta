package it.unibo.jakta.agents.bdi.dsl

import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.tuprolog.core.Term

fun main() {
    mas {
        agent("pippo") {
            actions {
                action("isThisAString", 1) {
                    val arg = argument<Term>(0)
                    println(arg.isAtom)
                }
            }
            beliefs {
                fact("love"("ice-cream"))
                fact("favNumber"(5))
            }
            goals {
                achieve("init")
            }
            plans {
                +achieve("init") onlyIf { "love"(X).fromSelf and "favNumber"(Y).fromSelf } then {
                    execute("isThisAString"(X))
                    execute("isThisAString"(Y))
                }
            }
        }
    }.start()
}
