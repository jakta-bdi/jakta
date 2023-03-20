package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.fsm.time.SimulatedTime
import io.github.anitvam.agents.fsm.time.Time

fun main() {
    mas {
        agent("alice") {
            goals {
                achieve("time")
            }
            actions {
                action("time", 0) {
                    println("time: ${this.requestTimestamp}")
                }
            }
            plans {
                + achieve("time") then {
                    iact("time")
                    achieve("time")
                }
            }
            timeDistribution {
                Time.continuous((it as SimulatedTime).value + 5.0)
            }
        }
        executionStrategy {
            ExecutionStrategy.oneThreadPerMas()
        }
    }.start()
}
