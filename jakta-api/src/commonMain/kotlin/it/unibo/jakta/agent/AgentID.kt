package it.unibo.jakta.agent

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Unique identifier for an Agent.
 * @param[name] optional name of the agent.
 * @param[id] the identifier as a string.
 */
data class AgentID(private val name: String? = null, private val id: String = generateId()) {
    private companion object {
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }

    /**
     * The display name of the agent, which is either its name or its id if no name is set.
     */
    val displayName: String get() = name ?: "Agent-$id"

    override fun equals(other: Any?): Boolean = other is AgentID && other.id == this.id

    override fun hashCode(): Int = id.hashCode()
}
