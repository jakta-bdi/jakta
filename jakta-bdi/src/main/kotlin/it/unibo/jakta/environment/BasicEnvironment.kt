package it.unibo.jakta.environment

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentID
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.environment.impl.EnvironmentImpl
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.messages.Message
import it.unibo.jakta.messages.MessageQueue
import it.unibo.jakta.perception.Perception

interface BasicEnvironment {

    val debugEnabled: Boolean

    val controller: Activity.Controller

    val agentIDs: Map<String, AgentID>

    val externalActions: Map<String, ExternalAction>

    val messageBoxes: Map<AgentID, MessageQueue>

    val data: Map<String, Any>

    val perception: Perception

    fun getNextMessage(agentName: String): Message?

    fun popMessage(agentName: String): BasicEnvironment

    fun submitMessage(agentName: String, message: Message): BasicEnvironment

    fun broadcastMessage(message: Message): BasicEnvironment

    fun addAgent(agent: ASAgent): BasicEnvironment

    fun removeAgent(agentName: String): BasicEnvironment

    fun percept(): ASBeliefBase = perception.percept()

    fun addData(key: String, value: Any): BasicEnvironment

    fun removeData(key: String): BasicEnvironment

    fun updateData(newData: Map<String, Any>): BasicEnvironment

    fun copy(
        agentIDs: Map<String, AgentID> = this.agentIDs,
        externalActions: Map<String, ExternalAction> = this.externalActions,
        messageBoxes: Map<AgentID, MessageQueue> = this.messageBoxes,
        perception: Perception = this.perception,
        data: Map<String, Any> = this.data,
    ): BasicEnvironment

    companion object {

        fun of(
            agentIDs: Map<String, AgentID> = emptyMap(),
            externalActions: Map<String, ExternalAction> = emptyMap(),
            messageBoxes: Map<AgentID, MessageQueue> = emptyMap(),
            perception: Perception = Perception.empty(),
            data: Map<String, Any> = emptyMap(),
        ): BasicEnvironment = EnvironmentImpl(
            externalActions,
            agentIDs,
            messageBoxes,
            perception,
            data,
        )
    }
}
