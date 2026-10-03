package it.unibo.jakta

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.util.ClassPathScanner
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAlchemistProperties {
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
