package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.node.Node

/**
 * An in-memory space for agents with [Situated] bodies, which can be shared by the nodes of a MAS.
 * The position of an agent is the one of its body. Agents are neighbors when they are within [range].
 * Install the skills on the agents of each node with `context(space.skillsFor(node)) { ... }`.
 *
 * ponytail: not thread-safe and neighbors are a linear scan of all agents;
 * run the MAS on a single thread (e.g. in `runBlocking`), use a spatial index if it gets large.
 */
class InMemorySpace(private val range: Double) {

    private val nodes = mutableListOf<Node<*>>()
    private val properties = mutableMapOf<AgentID, MutableMap<String, Any?>>()
    private val skills = Skills()

    /**
     * The skills for the agents of [node], which then shares this space with the other nodes.
     */
    fun skillsFor(node: Node<*>): Skills {
        if (node !in nodes) {
            nodes += node
        }
        return skills
    }

    private fun bodyOf(id: AgentID): Situated? = nodes.firstNotNullOfOrNull { it.agents[id] as? Situated }

    /**
     * The implementation of the skills on this space.
     */
    inner class Skills :
        SpatialSkill,
        NeighborhoodSkill,
        PropertySkill {
        override val Agent.position: Coordinates
            get() = body.position

        override fun Agent.moveTo(position: Coordinates) {
            body.position = position
        }

        override val Agent.neighbors: Set<AgentID>
            get() {
                val here = position
                val nearby = nodes.flatMap { it.agents.entries }.filter { (_, body) ->
                    body is Situated && body.position.distanceTo(here) <= range
                }
                return nearby.mapTo(mutableSetOf()) { it.key } - id
            }

        override fun Agent.property(name: String): Any? = propertiesOf(id)[name]

        override fun Agent.setProperty(name: String, value: Any?) {
            if (value == null) propertiesOf(id) -= name else propertiesOf(id)[name] = value
        }

        private val Agent.body: Situated
            get() = checkNotNull(bodyOf(id)) { "Agent ${id.displayName} has no position: embody it as Situated" }

        private fun propertiesOf(id: AgentID) =
            properties.getOrPut(id) { bodyOf(id)?.initialProperties.orEmpty().toMutableMap() }
    }
}
