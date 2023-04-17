package it.unibo.jakta.agents.bdi.dsl.examples

import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction
import it.unibo.jakta.agents.bdi.dsl.internalAction
import it.unibo.jakta.agents.bdi.dsl.mas

fun main() {
    val externalAction = object : AbstractExternalAction("ex_action", 0) {
        override fun action(request: ExternalRequest) {
            println("This is an action defined with the library model")
        }
    }

    val internalAction = internalAction("in_action", 0) {
        println("This is an action defined with the dsl")
    }

    mas {
        environment {
            actions {
                action(externalAction)
            }
        }
        agent("alice") {
            goals {
                achieve("run_external_action")
                achieve("run_internal_action")
            }
            actions {
                action(internalAction)
            }
            plans {
                + achieve("run_external_action") then {
                    execute("ex_action")
                    iact("in_action")
                }
            }
        }
    }.start()
}
