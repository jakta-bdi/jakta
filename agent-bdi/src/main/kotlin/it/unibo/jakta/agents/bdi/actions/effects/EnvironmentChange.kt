package it.unibo.jakta.agents.bdi.actions.effects

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.messages.Message

sealed interface EnvironmentChange : SideEffect
data class SpawnAgent(val agent: Agent) : EnvironmentChange
data class RemoveAgent(val agentName: String) : EnvironmentChange
data class SendMessage(
    val message: Message,
    val recipient: String,
) : EnvironmentChange
data class BroadcastMessage(val message: Message) : EnvironmentChange

data class PopMessage(val agentName: String) : EnvironmentChange

data class AddData(val key: String, val value: Any) : EnvironmentChange
data class RemoveData(val key: String) : EnvironmentChange
data class UpdateData(val newData: Map<String, Any>) : EnvironmentChange
