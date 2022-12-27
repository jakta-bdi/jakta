package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface ExecutionStrategy {
    fun dispatch(agent: Agent, environment: Environment): Runner
}

class SingleThreadedExecutionStrategy : ExecutionStrategy {
    override fun dispatch(agent: Agent, environment: Environment): Runner {
        val agentLC = AgentLifecycle.of(agent)
        return Runner.threadOf(
            Activity.of {
                agentLC.reason(environment)
            }
        )
    }
}
