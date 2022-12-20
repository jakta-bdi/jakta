package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface Dispatcher {
    companion object {
        fun syncOf(environment: Environment) = Runner.threadOf(
            Activity.of {
                environment.runAgents()
            }
        )

        fun threadOf(environment: Environment) = Runner.threadOf(
            Activity.of {
                environment.runAgents()
            }
        )
    }
}
