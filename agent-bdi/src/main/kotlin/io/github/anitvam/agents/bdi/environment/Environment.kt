package io.github.anitvam.agents.bdi.environment

import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.environment.impl.EnvironmentImpl
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

interface Environment {
    val agentIDs: Set<AgentID>

    val externalActions: Map<String, ExternalAction>

    val messageBoxes: Map<AgentID, MessageQueue>

    fun getNextMessage(agent: AgentID): Message?

    fun submitMessage(agent: AgentID, message: Message): Environment

    fun broadcastMessage(message: Message): Environment

    fun addAgent(agentID: AgentID): Environment

    fun removeAgent(agentID: AgentID): Environment

    fun percept(): Perception

    fun copy(
        agentIDs: Set<AgentID> = this.agentIDs,
        externalActions: Map<String, ExternalAction> = this.externalActions,
        messageBoxes: Map<AgentID, MessageQueue> = this.messageBoxes,
    ): Environment = EnvironmentImpl(
        externalActions,
        agentIDs,
        messageBoxes,
    ) { this.percept() }

    companion object {
        fun of(
            externalActions: Map<String, ExternalAction> = emptyMap(),
            perceptInvocation: () -> Perception = { Perception.empty() },
        ): Environment = EnvironmentImpl(externalActions, perceptInvocation = perceptInvocation)
    }
}
