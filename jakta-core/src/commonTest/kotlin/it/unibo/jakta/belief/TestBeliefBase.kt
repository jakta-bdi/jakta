package it.unibo.jakta.belief

import it.unibo.jakta.event.UnlimitedChannelQueue
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBeliefBase {

    @Test
    fun aSnapshotDoesNotChangeWithTheBeliefBase() {
        val beliefBase = BeliefBaseFactory.of(UnlimitedChannelQueue(), listOf("a", "b"))
        val snapshot = beliefBase.snapshot()
        // Iterating a live view while changing the belief base would also throw ConcurrentModificationException
        snapshot.forEach { beliefBase.remove(it) }
        beliefBase.add("c")
        assertEquals(listOf("a", "b"), snapshot.toList())
    }
}
