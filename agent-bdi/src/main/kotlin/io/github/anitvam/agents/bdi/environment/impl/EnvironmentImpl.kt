package io.github.anitvam.agents.bdi.environment.impl

import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

internal data class EnvironmentImpl(
    override val externalActions: Map<String, ExternalAction>,
    override var agentIDs: Set<AgentID> = emptySet(),
    override var messageBoxes: Map<AgentID, MessageQueue> = mapOf(),
    private val perceptInvocation: () -> Perception
) : Environment {
    override fun getNextMessage(agent: AgentID): Message? = messageBoxes[agent]?.lastOrNull()

    override fun submitMessage(agent: AgentID, message: Message) {
        if (messageBoxes.contains(agent)) {
            messageBoxes = messageBoxes + mapOf(agent to messageBoxes[agent]!!.plus(message))
        }
    }

    override fun addAgent(agentID: AgentID) {
        agentIDs = agentIDs + agentID
        messageBoxes = messageBoxes + mapOf(agentID to emptyList())
    }

    override fun removeAgent(agentID: AgentID) {
        agentIDs = agentIDs - agentID
        messageBoxes - agentID
    }

    override fun percept(): Perception = perceptInvocation()
}
