package io.github.anitvam.agents.bdi.environment.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentID
import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.messages.MessageQueue
import io.github.anitvam.agents.bdi.perception.Perception

internal class EnvironmentImpl(
    override val externalActions: Map<String, ExternalAction>,
    override val agentIDs: Map<String, AgentID> = emptyMap(),
    override val messageBoxes: Map<AgentID, MessageQueue> = mapOf(),
    override val perception: Perception,
) : Environment {
    override fun getNextMessage(agentName: String): Message? = messageBoxes[agentIDs[agentName]]?.lastOrNull()

    override fun popMessage(agentName: String): Environment {
        val message = getNextMessage(agentName)
        return if (message != null) {
            copy(
                messageBoxes = messageBoxes + mapOf(
                    agentIDs[agentName]!! to messageBoxes[agentIDs[agentName]]!!.minus(message)
                )
            )
        } else this
    }

    override fun submitMessage(agentName: String, message: Message) =
        if (messageBoxes.contains(agentIDs[agentName])) {
            copy(
                messageBoxes = messageBoxes + mapOf(
                    agentIDs[agentName]!! to messageBoxes[agentIDs[agentName]]!!.plus(message)
                )
            )
        } else this

    override fun broadcastMessage(message: Message): Environment = copy(
        messageBoxes = messageBoxes.entries.associate { it.key to it.value + message },
    )

    override fun addAgent(agent: Agent) =
        if (!agentIDs.contains(agent.name)) {
            copy(
                agentIDs = agentIDs + mapOf(agent.name to agent.agentID),
                messageBoxes = messageBoxes + mapOf(agent.agentID to emptyList()),
            )
        } else this

    override fun removeAgent(agentName: String) =
        if (agentIDs.contains(agentName)) {
            copy(
                messageBoxes = messageBoxes - agentIDs[agentName]!!,
                agentIDs = agentIDs - agentName,
            )
        } else this

    override fun percept(): BeliefBase = perception.percept()
}
