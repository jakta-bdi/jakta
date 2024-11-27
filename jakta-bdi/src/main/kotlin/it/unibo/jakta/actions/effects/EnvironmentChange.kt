package it.unibo.jakta.actions.effects

import it.unibo.jakta.ASAgent
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.messages.Message

// Potrebbero diventare dipendenti dalle capabilities ?

sealed interface EnvironmentChange : AgentChange, ActionResult
data class SpawnAgent(val agent: ASAgent) : EnvironmentChange {
    override fun ASMutableAgentContext.apply(controller: Activity.Controller?) {
        TODO("Not yet implemented")
    }
}

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
