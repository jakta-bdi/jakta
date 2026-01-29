package it.unibo.jakta.agent.basImpl

import it.unibo.jakta.agent.AgentID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * @param[name] optional name of the agent.
 * @param[id] the identifier as a string.
 */
data class BaseAgentID(private val name: String? = null, private val id: String = generateId())
    : AgentID {
    private companion object {
        /**
         * Generates a new random [kotlin.uuid.Uuid].
         * @return a [String] representing the [kotlin.uuid.Uuid].
         */
        @OptIn(ExperimentalUuidApi::class)
        private fun generateId(): String = Uuid.random().toString()
    }

    /**
     * The display name of the agent, which is either its name or its id if no name is set.
     */
    override val displayName: String get() = name ?: "Agent-$id"
}
