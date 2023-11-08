package it.unibo.jakta.agents.distributed.remoteagent.impl

import it.unibo.jakta.agents.bdi.AgentID
import it.unibo.jakta.agents.distributed.remoteagent.RemoteAgent

internal data class RemoteAgentImpl(
    override val agentID: AgentID = AgentID(),
    override val name: String,
    override val tags: Map<String, Any> = emptyMap(),
) : RemoteAgent {
    override fun replaceTags(tags: Map<String, Any>): RemoteAgent =
        if (tags != this.tags) {
            copy(tags = tags)
        } else {
            this
        }
}
