package it.unibo.jakta.agent

import it.unibo.jakta.agent.basImpl.BaseAgentID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AgentIDTest {

    @Test
    fun testEquals() {
        val agent1 = BaseAgentID(name = "AgentOne", id = "id-123")
        val agent2 = BaseAgentID(name = "AgentTwo", id = "id-123")
        val agent3 = BaseAgentID(name = "AgentOne", id = "id-456")
        assertEquals(
            agent1,
            agent2,
            "The same ID should make AgentIDs equal",
        )
        assertNotEquals(
            agent1,
            agent3,
            "Same name should not make AgentIDs equal if IDs differ",
        )
    }

    @Test
    fun testHashCode() {
        val agent1 = BaseAgentID(name = "AgentOne", id = "id-123")
        val agent2 = BaseAgentID(name = "AgentTwo", id = "id-123")
        assertEquals(
            agent1.hashCode(),
            agent2.hashCode(),
            "The same ID should produce the same hash code",
        )
    }

    @Test
    fun testDisplayName() {
        val agentWithName = BaseAgentID(name = "AgentOne", id = "id-123")
        val agentWithoutName = BaseAgentID(id = "id-456")
        assertEquals(
            "AgentOne",
            agentWithName.displayName,
            "The display name should be the provided name",
        )
        assertEquals(
            "Agent-id-456",
            agentWithoutName.displayName,
            "The display name should default to 'Agent-' plus the ID",
        )
    }

    @Test
    fun testCreation() {
        val agent1 = BaseAgentID()
        val agent2 = BaseAgentID()

        assertNotEquals(
            agent1,
            agent2,
            "Two AgentIDs created without parameters should have different IDs",
        )
    }
}
