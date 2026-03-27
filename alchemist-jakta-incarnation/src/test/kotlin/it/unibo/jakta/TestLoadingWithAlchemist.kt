package it.unibo.jakta

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.ClassPathScanner
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestLoadingWithAlchemist {

    @Test
    fun testLoading() {
        ClassPathScanner.resourcesMatching(".*\\.ya?ml", "it.unibo.jakta").forEach { simulationFile ->
            val (fileName) = checkNotNull(Regex(".*/([^/]+?)$").matchEntire(simulationFile.path)).destructured
            testRunning(fileName, simulationFile)
        }
    }

    private fun testRunning(fileName: String, fileUrl: URL) {
        Logger.setMinSeverity(Severity.Verbose)
        println("Running $fileName")
        val loader = LoadAlchemist.from(fileUrl)
        assertNotNull(loader)
        val simulation = loader.getDefault<Any, Nothing>()
        assertNotNull(simulation)
        simulation.environment.addTerminator(AfterTime(DoubleTime(20.0)))
        simulation.play()
        simulation.run()
        simulation.error.ifPresent { throw it }

    }
}
