package io.github.anitvam.agents.bdi.actions.effects

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message

sealed interface EnvironmentChange : SideEffect
data class SpawnAgent(val agent: Agent) : EnvironmentChange
data class RemoveAgent(val agentID: AgentID) : EnvironmentChange
data class SendMessage(
    val message: Message,
    val recipient: AgentID,
) : EnvironmentChange
data class BroadcastMessage(val message: Message) : EnvironmentChange

// object Act : EnvironmentChange TODO("Serve?")
