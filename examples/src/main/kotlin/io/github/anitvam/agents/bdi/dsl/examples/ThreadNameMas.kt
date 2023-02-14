package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy

fun main() {
    mas {
        environment {
            actions {
                action("thread", 0) {
                    println("${this.sender} thread: ${Thread.currentThread().name}")
                }
            }
        }
        agent("alice") {
            goals {
                achieve("my_thread")
            }
            plans {
                + achieve("my_thread") then {
                    act("thread")
                }
            }
        }
        agent("bob") {
            goals {
                achieve("print_thread")
            }
            plans {
                + achieve("print_thread") then {
                    act("thread")
                }
            }
        }
        executionStrategy {
            ExecutionStrategy.oneThreadPerMas()
            // ExecutionStrategy.oneThreadPerAgent()
        }
    }.start()
}
