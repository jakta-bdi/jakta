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
        /**
         * Generates a new random [Uuid].
         * @return a [String] representing the [Uuid].
         */
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }

    /**
     * The display name of the agent, which is either its name or its id if no name is set.
     */
    val displayName: String get() = name ?: "Agent-$id"
}
