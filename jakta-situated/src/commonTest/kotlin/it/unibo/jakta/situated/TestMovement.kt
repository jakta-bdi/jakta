package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestMovement {

    private val agent = object : Agent {
        override val id = object : AgentID {
            override val displayName = "walker"
        }
    }

    private val space = object : SpatialSkill {
        var current = Coordinates(0.0, 0.0)
        override val Agent.position get() = current
        override fun Agent.moveTo(position: Coordinates) {
            current = position
        }
    }

    @Test
    fun testMoveTowardsCoversAtMostTheGivenDistance() = context(space) {
        val target = Coordinates(3.0, 4.0)
        assertFalse(agent.moveTowards(target, 2.5))
        assertEquals(Coordinates(1.5, 2.0), agent.position)
        assertTrue(agent.moveTowards(target, 2.5))
        assertEquals(target, agent.position)
    }

    @Test
    fun testMoveBy() = context(space) {
        agent.moveBy(Coordinates(1.0, -1.0))
        agent.moveBy(Coordinates(1.0, -1.0))
        assertEquals(Coordinates(2.0, -2.0), agent.position)
    }
}
