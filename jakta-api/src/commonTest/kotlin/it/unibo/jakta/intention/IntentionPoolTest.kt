package it.unibo.jakta.intention

import it.unibo.jakta.event.GoalAddEvent
import kotlin.reflect.typeOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest

class IntentionPoolTest {

    lateinit var intentionPool: MutableIntentionPool
    lateinit var agentJob: Job
    lateinit var intentionJob: Job
    lateinit var otherJob: Job
    lateinit var intention: Intention
    lateinit var otherIntention: Intention
    lateinit var newGoalEvent: GoalAddEvent<String, Unit>

    @BeforeTest
    fun init() {
        intentionPool = MutableIntentionPoolImpl(Channel())
        agentJob = SupervisorJob()
        intentionJob = Job(agentJob)
        otherJob = Job(agentJob)
        intention = Intention(job = intentionJob)
        otherIntention = Intention(job = otherJob)
        newGoalEvent = GoalAddEvent(
            goal = "NewGoal",
            resultType = typeOf<Unit>(),
            completion = CompletableDeferred(),
            intention = null,
        )
    }

    @Test
    fun testInsertion() {
        assertTrue("can insert intentions into the pool") {
            intentionPool.tryPut(intention)
        }

        assertFalse("cannot insert twice the same") {
            intentionPool.tryPut(intention)
        }

        assertContains(
            intentionPool.getIntentionsSet(),
            intention,
            "the inserted intention is present in the pool",
        )

        assertEquals(
            1,
            intentionPool.getIntentionsSet().size,
            "the pool contains exactly one intention",
        )
    }

    @Test
    fun testDrop() = runTest {
        assertTrue {
            intentionPool.tryPut(intention)
        }

        assertFalse("cannot drop a non-existing intention") {
            intentionPool.drop(otherIntention.id)
        }

        assertTrue("can drop a previously added intention") {
            intentionPool.drop(intention.id)
        }

        assertEquals(
            0,
            intentionPool.getIntentionsSet().size,
            "the pool is empty after dropping the only intention",
        )

        assertTrue(
            intention.job.isCancelled,
            "the dropped intention's job is cancelled",
        )

        assertFalse(
            otherIntention.job.isCancelled,
            "other intentions' jobs are not cancelled",
        )

        assertFalse(
            agentJob.isCancelled,
            "the agent job is not cancelled",
        )
    }
//
//    should("create new intention when an event has no reference") {
//        runTest {
//            launch(agentJob) {
//                val next = intentionPool.nextIntention(newGoalEvent)
//                agentJob.children.shouldContain(next.job.parent)
//
//                val otherNext = intentionPool.nextIntention(newGoalEvent)
//                agentJob.children.shouldContain(otherNext.job.parent)
//
//                next.job shouldNotBe otherNext.job
//                next.job.parent shouldBe otherNext.job.parent
//            }
//        }
//    }
//
//    should("reuse an existing intention when an event references it") {
//        intentionPool.tryPut(intention)
//
//        val event = newGoalEvent.copy(intention = intention)
//
//        runTest {
//            launch(agentJob) {
//                val next = intentionPool.nextIntention(event)
//                next shouldBe intention
//                next.job shouldBe intention.job
//                intentionPool.getIntentionsSet().size shouldBe 1
//            }
//        }
//    }
//
//    should("cancel all intentions when the agent job is cancelled") {
//        runTest {
//            launch(agentJob) {
//                val i1 = intentionPool.nextIntention(newGoalEvent)
//                val i2 = intentionPool.nextIntention(newGoalEvent)
//                val sub = intentionPool.nextIntention(newGoalEvent.copy(intention = i1))
//
//                agentJob.cancelAndJoin()
//
//                i1.job.isCancelled.shouldBeTrue()
//                i2.job.isCancelled.shouldBeTrue()
//                sub.job.isCancelled.shouldBeTrue()
//                intentionPool.getIntentionsSet().shouldBeEmpty()
//            }
//        }
//    }
}
