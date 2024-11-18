package it.unibo.jakta.environment

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.environment.impl.EnvironmentImpl
import it.unibo.jakta.messages.Message
import it.unibo.jakta.messages.MessageQueue
import it.unibo.jakta.perception.Perception

interface Environment {
    val agentIDs: Map<String, AgentID>

    val externalActions: Map<String, ExternalAction>

    val messageBoxes: Map<AgentID, MessageQueue>

    val data: Map<String, Any>

    val perception: Perception

    fun getNextMessage(agentName: String): Message?

    fun popMessage(agentName: String): Environment

    fun submitMessage(agentName: String, message: Message): Environment

    fun broadcastMessage(message: Message): Environment

    fun addAgent(agent: ASAgent): Environment

    fun removeAgent(agentName: String): Environment

    fun percept(): ASBeliefBase = perception.percept()

    fun addData(key: String, value: Any): Environment

    fun removeData(key: String): Environment

    fun updateData(newData: Map<String, Any>): Environment

    fun copy(
        agentIDs: Map<String, AgentID> = this.agentIDs,
        externalActions: Map<String, ExternalAction> = this.externalActions,
        messageBoxes: Map<AgentID, MessageQueue> = this.messageBoxes,
        perception: Perception = this.perception,
        data: Map<String, Any> = this.data,
    ): Environment

    companion object {

        fun of(
            agentIDs: Map<String, AgentID> = emptyMap(),
            externalActions: Map<String, ExternalAction> = emptyMap(),
            messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
            perception: Perception = Perception.empty(),
            data: Map<String, Any> = emptyMap(),
        ): Environment = EnvironmentImpl(
            externalActions,
            agentIDs,
            messageBoxes,
            perception,
            data,
        )
    }
}
