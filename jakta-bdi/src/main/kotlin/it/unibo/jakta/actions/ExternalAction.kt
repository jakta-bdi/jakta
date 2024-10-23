package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.messages.Message

interface ExternalAction : Action<EnvironmentChange, ExternalResponse, ExternalRequest> {
    fun addAgent(agent: Agent)
    fun removeAgent(agentName: String)
    fun sendMessage(agentName: String, message: Message)
    fun broadcastMessage(message: Message)
    fun addData(key: String, value: Any)
    fun removeData(key: String)
    fun updateData(newData: Map<String, Any>)

    fun updateData(keyValue: Pair<String, Any>, vararg others: Pair<String, Any>) =
        updateData(mapOf(keyValue, *others))
}