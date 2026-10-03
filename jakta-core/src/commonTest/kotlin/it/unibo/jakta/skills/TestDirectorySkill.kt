package it.unibo.jakta.skills

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import kotlin.test.Test
import kotlin.test.assertEquals

class TestDirectorySkill {

    // agents are known to their node as soon as it is built, before any of them runs
    private val nodeA = node(NodeBuilders.baseNode()) {
        agent<Any, Any>("worker") { embodiedAs { Any() } }
        agent<Any, Any>("manager") { embodiedAs { Any() } }
    }

    private val nodeB = node(NodeBuilders.baseNode()) {
        agent<Any, Any>("worker") { embodiedAs { Any() } }
    }

    private fun AgentID.asAgent() = object : Agent {
        override val id = this@asAgent
    }

    @Test
    fun `agents with the same name on different nodes have distinct ids and are all found`() {
        val directory = InMemoryDirectory()
        val skill = directory.skillFor(nodeA)
        directory.skillFor(nodeB)
        val manager = nodeA.agents.keys.single { it.name == "manager" }.asAgent()
        val workers = with(skill) { manager.lookup("worker") }
        assertEquals(2, workers.size)
        assertEquals(nodeA.agents.keys + nodeB.agents.keys - manager.id, workers)
        assertEquals(emptySet(), with(skill) { manager.lookup("nobody") })
    }
}
