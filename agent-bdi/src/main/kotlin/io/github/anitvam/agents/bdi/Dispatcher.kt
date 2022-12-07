package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface Dispatcher {
    companion object {
        fun syncOf(agent: Agent): Runner {
            val agentLC = AgentLifecycle.of(agent)
            return Runner.threadOf(
                Activity.of {
                    agentLC.reason()
                }
            )
        }

        fun threadOf(agent: Agent): Runner {
            val agentLC = AgentLifecycle.of(agent)
            return Runner.threadOf(
                Activity.of {
                    agentLC.reason()
                }
            )
        }
    }
}
