package io.github.anitvam.agents.bdi.environment.impl

import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

internal data class EnvironmentImpl(
    override val externalActions: Map<String, ExternalAction>,
    override val agentIDs: Set<AgentID> = emptySet(),
    override val messageBoxes: Map<AgentID, MessageQueue> = mapOf(),
    private val perceptInvocation: () -> Perception
) : Environment {
    override fun getNextMessage(agent: AgentID): Message? = messageBoxes[agent]?.lastOrNull()

    override fun submitMessage(agent: AgentID, message: Message) =
        if (messageBoxes.contains(agent)) {
            copy(
                messageBoxes = messageBoxes + mapOf(agent to messageBoxes[agent]!!.plus(message)),
            )
        } else this

    override fun broadcastMessage(message: Message): Environment = copy(
        messageBoxes = messageBoxes.entries.associate { it.key to it.value + message },
    )

    override fun addAgent(agentID: AgentID) =
        if (!agentIDs.contains(agentID)) {
            copy(
                agentIDs = agentIDs + agentID,
                messageBoxes = messageBoxes + mapOf(agentID to emptyList()),
            )
        } else this

    override fun removeAgent(agentID: AgentID) =
        if (agentIDs.contains(agentID)) {
            copy(
                agentIDs = agentIDs - agentID,
                messageBoxes = messageBoxes - agentID,
            )
        } else this

    override fun percept(): Perception = perceptInvocation()
}
