package it.unibo.jakta.intention

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.Job

class IntentionTest {

    @Test
    fun testEquality() {
        val id = IntentionID()
        val intention1 = Intention(id = id, job = Job())
        val intention2 = Intention(id = id, job = Job())
        val intention3 = Intention(id = IntentionID(), job = Job())

        assertEquals(
            intention1,
            intention2,
            "Intentions with the same ID should be equal",
        )

        assertNotEquals(
            intention1,
            intention3,
            "Intentions with different IDs should not be equal",
        )
    }

    // TODO more tests for intentions
}
