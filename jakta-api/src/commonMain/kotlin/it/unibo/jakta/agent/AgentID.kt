package it.unibo.jakta.agent

import kotlin.uuid.Uuid

/**
 * Unique identifier for an [Agent].
 *
 * The [uuid] makes the identifier unique across all the nodes of a MAS, even remote ones,
 * so two agents never clash even if they share the same [name].
 * Identifiers are created by the framework when an agent is built:
 * other agents learn them from [it.unibo.jakta.event.AgentEvent.External.Message.sender] or a directory skill.
 * @param name the human-readable name of the agent, not necessarily unique.
 * @param uuid the universally unique part of the identifier.
 */
data class AgentID(val name: String, val uuid: Uuid) {
    override fun toString(): String = name
}
