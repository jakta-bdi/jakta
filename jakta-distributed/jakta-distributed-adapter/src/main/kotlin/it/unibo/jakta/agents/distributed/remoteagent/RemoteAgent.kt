package it.unibo.jakta.agents.distributed.remoteagent

import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.distributed.remoteagent.impl.RemoteAgentImpl
import it.unibo.tuprolog.utils.Taggable
import java.util.*

/**
 * A remote agent is an agent that is not part of the current environment, but is reachable through the network.
 * It is identified by an [agentID] and a [name].
 */
interface RemoteAgent : Taggable<RemoteAgent> {
    val agentID: AgentID
    val name: String

    companion object {
        fun of(
            agentID: AgentID = AgentID(),
            name: String = "Agent-" + UUID.randomUUID()
        ): RemoteAgent = RemoteAgentImpl(agentID, name)
    }
}
