package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.actions.effects.BroadcastMessage
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.PopMessage
import io.github.anitvam.agents.bdi.actions.effects.RemoveAgent
import io.github.anitvam.agents.bdi.actions.effects.SendMessage
import io.github.anitvam.agents.bdi.actions.effects.SpawnAgent
import io.github.anitvam.agents.bdi.environment.Environment

internal class MasImpl(
    override val executionStrategy: ExecutionStrategy,
    override var environment: Environment,
    override var agents: Iterable<Agent>,
) : Mas {
    init {
        agents.forEach { environment = environment.addAgent(it) }
    }

    override fun start() = executionStrategy.dispatch(this)

    override fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>) = effects.forEach {

        when (it) {
            is BroadcastMessage -> environment = environment.broadcastMessage(it.message)
            is RemoveAgent -> environment = environment.removeAgent(it.agentName)
            is SendMessage -> environment = environment.submitMessage(it.recipient, it.message)
            is SpawnAgent -> {
                agents += it.agent
                environment = environment.addAgent(it.agent)
            }
            is PopMessage -> environment = environment.popMessage(it.agentName)
        }
    }
}
