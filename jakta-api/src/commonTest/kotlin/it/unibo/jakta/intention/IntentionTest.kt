package it.unibo.jakta.intention

import it.unibo.jakta.intention.baseImpl.BaseIntention
import it.unibo.jakta.intention.baseImpl.BaseIntentionID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlinx.coroutines.Job

class IntentionTest {

    @Test
    fun testEquality() {
        val id = BaseIntentionID()
        val intention1 = BaseIntention(id = id, job = Job())
        val intention2 = BaseIntention(id = id, job = Job())
        val intention3 = BaseIntention(id = BaseIntentionID(), job = Job())

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
