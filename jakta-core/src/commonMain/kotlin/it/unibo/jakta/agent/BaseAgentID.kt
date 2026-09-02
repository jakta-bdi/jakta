package it.unibo.jakta.agent

import kotlin.uuid.Uuid

/**
 * @param[name] optional name of the agent.
 * @param[id] the identifier as a string.
 */
class BaseAgentID(private val name: String? = null, private val id: String = Uuid.random().toString()) : AgentID {
    /**
     * The display name of the agent, which is either its name or its id if no name is set.
     */
    override val displayName: String get() = (name ?: "Agent-$id")

    override fun equals(other: Any?): Boolean =
        this === other || other is BaseAgentID && id == other.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id
}
