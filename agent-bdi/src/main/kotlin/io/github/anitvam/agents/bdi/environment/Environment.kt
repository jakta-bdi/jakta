package io.github.anitvam.agents.bdi.environment

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.impl.EnvironmentImpl
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

interface Environment {
    val agentIDs: Map<String, AgentID>

    val externalActions: Map<String, ExternalAction>

    val messageBoxes: Map<AgentID, MessageQueue>

    val perception: Perception

    fun getNextMessage(agentName: String): Message?

    fun popMessage(agentName: String): Environment

    fun submitMessage(agentName: String, message: Message): Environment

    fun broadcastMessage(message: Message): Environment

    fun addAgent(agent: Agent): Environment

    fun removeAgent(agentName: String): Environment

    fun percept(): BeliefBase = perception.percept()

    fun copy(
        agentIDs: Map<String, AgentID> = this.agentIDs,
        externalActions: Map<String, ExternalAction> = this.externalActions,
        messageBoxes: Map<AgentID, MessageQueue> = this.messageBoxes,
        perception: Perception = this.perception,
    ): Environment = EnvironmentImpl(
        externalActions,
        agentIDs,
        messageBoxes,
        perception,
    )

    companion object {
        fun of(
            externalActions: Map<String, ExternalAction> = emptyMap(),
            perception: Perception = Perception.empty(),
        ): Environment = EnvironmentImpl(externalActions, perception = perception)
    }
}
