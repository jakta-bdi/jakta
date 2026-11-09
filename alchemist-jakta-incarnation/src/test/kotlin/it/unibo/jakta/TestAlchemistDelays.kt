package it.unibo.jakta

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.ClassPathScanner
import it.unibo.jakta.alchemist.simulatedTimeAfter
import it.unibo.jakta.test.waitStartedAt
import it.unibo.jakta.test.waitTimedOutAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestAlchemistDelays {
    @Test
    fun delaysAreNotRoundedToWholeSeconds() {
        assertEquals(10.5, simulatedTimeAfter(10.0, 500))
        assertEquals(11.5, simulatedTimeAfter(10.0, 1500))
    }

    @Test
    fun timeoutsFollowSimulatedTime() {
        val simulationFile = ClassPathScanner.resourcesMatching(".*agent-with-timeout.ya?ml", "it.unibo.jakta").single()
        val simulation = LoadAlchemist.from(simulationFile).getDefault<Any?, Nothing>()
        simulation.environment.addTerminator(AfterTime(DoubleTime(20.0)))
        simulation.play()
        simulation.run()
        simulation.error.ifPresent { throw it }
        // the agent steps every 0.1 s: a 5 s timeout fires at the first step 5 s after the wait started
        val startedAt = assertNotNull(waitStartedAt)
        val timedOutAt = assertNotNull(waitTimedOutAt, "the timeout did not fire in simulated time")
        assertEquals(5.0, timedOutAt - startedAt, absoluteTolerance = 0.2)
    }
}
