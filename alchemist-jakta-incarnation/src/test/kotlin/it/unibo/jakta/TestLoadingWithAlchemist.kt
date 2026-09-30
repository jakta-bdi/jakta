package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.ClassPathScanner
import it.unibo.jakta.dsl.alchemistNode
import it.unibo.jakta.dsl.device
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.test.PING
import it.unibo.jakta.test.PONG
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class TestLoadingWithAlchemist {

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Info)
    }

    @Test
    fun testCommunicationInSameNode() {
        assertTrue(run("two-agents-same-node").contains(PONG))
    }

    @Test
    fun testDeviceHostsOneJaktaNodePerBlock() {
        val device = device(NodeBuilders.alchemistNode<Any>()) {
            node { }
            node { }
        }
        assertEquals(2, device.nodes.size)
    }

    @Test
    fun testCommunicationBetweenNodesOfTheSameDevice() {
        assertTrue(run("two-nodes-same-device").contains(PONG))
    }

    @Test
    fun testGlobalCommunicationIgnoresTheNetwork() {
        assertTrue(run("two-agents-two-nodes").contains(PONG))
    }

    @Test
    fun testNeighborhoodCommunicationOutOfRange() {
        val simulation = run("two-agents-two-nodes", "messaging" to "neighborhood")
        assertFalse(simulation.contains(PING))
        assertFalse(simulation.contains(PONG))
    }

    @Test
    fun testNeighborhoodCommunicationInRange() {
        assertTrue(run("two-agents-two-nodes", "messaging" to "neighborhood", "range" to 10).contains(PONG))
    }

    private fun Simulation<Any?, *>.contains(molecule: Molecule) = environment.nodes.any { it.contains(molecule) }

    private fun run(name: String, vararg variables: Pair<String, Any>): Simulation<Any?, *> {
        val simulationFile = ClassPathScanner
            .resourcesMatching(".*/$name.ya?ml", "it.unibo.jakta")
            .singleOrNull() ?: fail("No single yaml file matching name { $name } found.")
        val simulation = LoadAlchemist.from(simulationFile).getWith<Any?, Nothing>(variables.toMap())
        simulation.environment.addTerminator(AfterTime(DoubleTime(20.0)))
        simulation.play()
        simulation.run()
        simulation.error.ifPresent { throw it }
        return simulation
    }
}
