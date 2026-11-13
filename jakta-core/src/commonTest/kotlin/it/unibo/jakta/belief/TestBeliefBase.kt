package it.unibo.jakta.belief

import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    /**
     * A revision keeping a single reading per sensor: `temp=21` replaces `temp=20`.
     */
    private val latestReading = object : BeliefRevision<String> {
        private fun Collection<String>.sameSensor(reading: String) = filter {
            it.substringBefore('=') == reading.substringBefore('=') && it != reading
        }

        override fun add(current: Collection<String>, toAdd: String): Revision<String> = when (toAdd) {
            in current -> Revision()

            else -> current.sameSensor(toAdd).let { old ->
                Revision(add = listOf(toAdd), remove = old, announceAdded = listOf(toAdd), announceRemoved = old)
            }
        }
    }

    private fun UnlimitedChannelQueue<AgentEvent.Internal.Belief<String>>.drain() =
        generateSequence { tryNext() }.toList()

    @Test
    fun theDefaultRevisionTreatsBeliefsAsASet() {
        val events = UnlimitedChannelQueue<AgentEvent.Internal.Belief<String>>()
        val beliefBase = BeliefBaseFactory.of(events, listOf("a"))
        assertEquals(false, beliefBase.add("a"))
        assertEquals(true, beliefBase.add("b"))
        assertEquals(true, beliefBase.remove("a"))
        assertEquals(false, beliefBase.remove("c"))
        assertEquals(setOf("b"), beliefBase.snapshot().toSet())
        assertEquals(listOf(BeliefAddEvent("b"), BeliefRemoveEvent("a")), events.drain())
    }

    @Test
    fun aRevisionDecidesWhatIsStoredAndWhichEventsAreRaised() {
        val events = UnlimitedChannelQueue<AgentEvent.Internal.Belief<String>>()
        // the initial beliefs are revised too, without events
        val beliefBase = BeliefBaseFactory.of(events, listOf("temp=19", "temp=20", "door=open"), latestReading)
        assertEquals(setOf("temp=20", "door=open"), beliefBase.snapshot().toSet())
        assertEquals(emptyList(), events.drain())

        beliefBase.add("temp=21")
        assertEquals(setOf("temp=21", "door=open"), beliefBase.snapshot().toSet())
        assertEquals(listOf(BeliefRemoveEvent("temp=20"), BeliefAddEvent("temp=21")), events.drain())
    }

    @Test
    fun aRevisionCanReplaceABeliefWithAnEqualOne() {
        // equal beliefs, told apart by something their equality ignores
        data class Reading(val sensor: String, val source: String) {
            override fun equals(other: Any?) = other is Reading && other.sensor == sensor
            override fun hashCode() = sensor.hashCode()
        }
        val replacing = object : BeliefRevision<Reading> {
            override fun add(current: Collection<Reading>, toAdd: Reading) =
                Revision(add = listOf(toAdd), announceAdded = listOf(toAdd))
        }
        val beliefBase = BeliefBaseFactory.of(UnlimitedChannelQueue(), listOf(Reading("temp", "alice")), replacing)
        beliefBase.add(Reading("temp", "carol"))
        assertEquals("carol", beliefBase.snapshot().single().source)
    }

    @Test
    fun theDefaultRevisionReplacesTheBeliefsInScope() {
        val events = UnlimitedChannelQueue<AgentEvent.Internal.Belief<String>>()
        val beliefBase = BeliefBaseFactory.of(events, listOf("at(1)", "door=open", "charge=3"))
        // the beliefs in scope are now at(2) and charge=3: at(1) goes, at(2) comes, charge=3 is unchanged
        val inScope: (String) -> String? = { belief ->
            belief.takeIf { it.startsWith("at(") || it.startsWith("charge=") }
        }
        assertEquals(true, beliefBase.replace(listOf("at(2)", "charge=3"), inScope))
        assertEquals(setOf("at(2)", "door=open", "charge=3"), beliefBase.snapshot().toSet())
        assertEquals(listOf(BeliefRemoveEvent("at(1)"), BeliefAddEvent("at(2)")), events.drain())
        // the same replacement again changes nothing
        assertEquals(false, beliefBase.replace(listOf("at(2)", "charge=3"), inScope))
        assertEquals(emptyList(), events.drain())
    }

    @Test
    fun replacementsMustBeInTheirScope() {
        val beliefBase = BeliefBaseFactory.of(UnlimitedChannelQueue(), listOf("at(1)"))
        val atScope: (String) -> String? = { belief -> belief.takeIf { it.startsWith("at(") } }
        assertFailsWith<IllegalArgumentException> { beliefBase.replace(listOf("door=open"), atScope) }
        assertEquals(setOf("at(1)"), beliefBase.snapshot().toSet())
    }
}
