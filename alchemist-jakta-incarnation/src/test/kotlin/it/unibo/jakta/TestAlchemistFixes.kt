package it.unibo.jakta

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.util.ClassPathScanner
import it.unibo.jakta.alchemist.simulatedTimeAfter
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAlchemistFixes {
    @Test
    fun delaysAreNotRoundedToWholeSeconds() {
        assertEquals(10.5, simulatedTimeAfter(10.0, 500))
        assertEquals(11.5, simulatedTimeAfter(10.0, 1500))
    }

    @Test
    fun exportingAMoleculeWithoutPropertyDoesNotFail() {
        val simulationFile = ClassPathScanner.resourcesMatching(
            ".*two-agents-same-node.ya?ml",
            "it.unibo.jakta",
        ).single()
        val environment = LoadAlchemist.from(simulationFile).getDefault<Any?, Nothing>().environment
        // Alchemist exporters read concentrations through getProperty passing no property
        val property = environment.incarnation.getProperty(environment.nodes.first(), SimpleMolecule("missing"), null)
        assertEquals(Double.NaN, property)
    }
}
