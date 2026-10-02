package it.unibo.jakta.belief

import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
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

    /**
     * A revision keeping a single reading per sensor: `temp=21` replaces `temp=20`.
     */
    private val latestReading = object : BeliefRevision<String> {
        private fun Collection<String>.sameSensor(reading: String) = filter {
            it.substringBefore('=') == reading.substringBefore('=') && it != reading
        }

        override fun add(beliefs: Collection<String>, belief: String): Revision<String> = when (belief) {
            in beliefs -> Revision()

            else -> beliefs.sameSensor(belief).let { old ->
                Revision(stored = listOf(belief), dropped = old, added = listOf(belief), removed = old)
            }
        }

        override fun remove(beliefs: Collection<String>, belief: String): Revision<String> =
            BeliefRevision.plain<String>().remove(beliefs, belief)
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
            override fun add(beliefs: Collection<Reading>, belief: Reading) =
                Revision(stored = listOf(belief), added = listOf(belief))

            override fun remove(beliefs: Collection<Reading>, belief: Reading) =
                BeliefRevision.plain<Reading>().remove(beliefs, belief)
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
        val inScope: (String) -> Boolean = { it.startsWith("at(") || it.startsWith("charge=") }
        assertEquals(true, beliefBase.replace(inScope, listOf("at(2)", "charge=3")))
        assertEquals(setOf("at(2)", "door=open", "charge=3"), beliefBase.snapshot().toSet())
        assertEquals(listOf(BeliefRemoveEvent("at(1)"), BeliefAddEvent("at(2)")), events.drain())
        // the same replacement again changes nothing
        assertEquals(false, beliefBase.replace(inScope, listOf("at(2)", "charge=3")))
        assertEquals(emptyList(), events.drain())
    }
}
