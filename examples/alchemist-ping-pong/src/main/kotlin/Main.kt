// Alchemist deprecated its Swing viewer in favour of a web UI that is not released yet: Swing is still the only viewer
@file:Suppress("DEPRECATION")

import ch.qos.logback.classic.Level
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.slf4j.LoggerFactory

/**
 * Loads the simulation of `ping-pong.yml`, where Alice and Bob play ping-pong from two devices, and shows it in
 * Alchemist's viewer: what the agents print goes to the console.
 */
fun main() {
    Logger.setMinSeverity(Severity.Assert)
    // Alchemist logs a lot while loading: only its warnings and errors
    (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger).level = Level.WARN
    val simulation = LoadAlchemist.from(checkNotNull(SwingGUI::class.java.classLoader.getResource("ping-pong.yml")))
        .getDefault<Any, Euclidean2DPosition>()
    val viewer = SwingGUI<Any, Euclidean2DPosition>(simulation.environment)
    // one unit of simulated time per second, to follow the rally
    viewer.isRealTime = true
    simulation.addOutputMonitor(viewer)
    simulation.play()
    simulation.run()
    simulation.error.ifPresent { throw it }
}
