package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.node.Node

/**
 * A skill to find the [AgentID]s of the agents of a MAS by their name,
 * e.g. to send them messages without sharing their identifiers beforehand.
 * Names are not unique, so a lookup can find any number of agents.
 */
interface DirectorySkill {
    /**
     * The identifiers of the agents named [name] known to the directory.
     */
    fun Agent.lookup(name: String): Set<AgentID>
}

/**
 * The identifiers of the agents named [name], found by the [DirectorySkill] in scope.
 */
context(skill: DirectorySkill)
fun Agent.lookup(name: String): Set<AgentID> = with(skill) { lookup(name) }

/**
 * A directory of the agents hosted by the nodes that share it within the same process.
 * Install the skill on the agents of each node with `context(directory.skillFor(node)) { ... }`:
 * the agents of a node are found only once the node has been given to [skillFor].
 *
 * ponytail: a linear scan of all the agents of all the nodes, index them by name if lookups get frequent.
 */
class InMemoryDirectory {

    private val nodes = mutableListOf<Node<*>>()

    private val skill = object : DirectorySkill {
        override fun Agent.lookup(name: String): Set<AgentID> =
            nodes.flatMapTo(mutableSetOf()) { node -> node.agents.keys.filter { it.name == name } }
    }

    /**
     * The skill for the agents of [node], whose agents can then be found by the agents of all the nodes sharing it.
     */
    fun skillFor(node: Node<*>): DirectorySkill {
        if (node !in nodes) {
            nodes += node
        }
        return skill
    }
}
