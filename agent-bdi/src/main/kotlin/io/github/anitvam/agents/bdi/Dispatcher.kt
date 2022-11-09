package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface Dispatcher {
    companion object {
        fun syncOf(agent: Agent) = Runner.threadOf(
            Activity.of {
                AgentLifecycle.default(agent).reason()
            }
        )

        fun threadOf(agent: Agent) = Runner.threadOf(
            Activity.of {
                AgentLifecycle.default(agent).reason()
            }
        )
    }
}
