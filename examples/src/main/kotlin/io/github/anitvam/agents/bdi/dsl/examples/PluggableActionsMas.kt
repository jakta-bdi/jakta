package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.actions.impl.AbstractExternalAction
import io.github.anitvam.agents.bdi.dsl.internalAction
import io.github.anitvam.agents.bdi.dsl.mas

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
                    act("ex_action")
                    iact("in_action")
                }
            }
        }
    }.start()
}
