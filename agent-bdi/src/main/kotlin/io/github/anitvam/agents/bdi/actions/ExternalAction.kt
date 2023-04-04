package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange

interface ExternalAction : Action<EnvironmentChange, ExternalResponse, ExternalRequest> {
    fun addAgent(agent: Agent)
    fun removeAgent(agentName: String)
    fun sendMessage(agentName: String, message: Message)
    fun broadcastMessage(message: Message)
    fun addData(key: String, value: Any)
    fun removeData(key: String)
    fun updateData(newData: Map<String, Any>)
}
