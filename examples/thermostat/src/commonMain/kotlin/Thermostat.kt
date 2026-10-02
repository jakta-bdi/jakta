import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * How far from the target the temperature can drift before the thermostat reacts.
 */
private const val TOLERANCE = 0.5

/**
 * What the thermostat believes about the room.
 */
sealed interface RoomBelief

/** The temperature of the room, in [degrees]. */
data class Temperature(val degrees: Double) : RoomBelief

/** The temperature to keep, in [degrees]. */
data class Target(val degrees: Double) : RoomBelief

/** The [mode] of the heating and cooling system. */
data class Running(val mode: Mode) : RoomBelief

private val Collection<RoomBelief>.target get() = filterIsInstance<Target>().single().degrees
private val Collection<RoomBelief>.mode get() = filterIsInstance<Running>().single().mode

private fun Double.roundedToTenths() = (this * 10).roundToInt() / 10.0

/**
 * A thermostat keeping the room within [TOLERANCE] degrees of its target, heating or cooling it with [hvac].
 */
fun thermostat(hvac: Hvac) = agent<RoomBelief, String, Any>(BaseAgentID("thermostat")) {
    embodiedAs { Any() }
    handlesPerceptionEvents { perception ->
        when (perception) {
            is RoomReading -> {
                val readings = setOf(
                    Temperature(perception.temperature.roundedToTenths()),
                    Target(perception.target),
                    Running(perception.mode),
                )
                AgentUpdate.Belief(readings - beliefs.toSet(), beliefs.toSet() - readings)
            }

            else -> null
        }
    }
    hasPlanLibrary {
        adding.belief {
            this as? Temperature
        } onlyWhen {
            context.takeIf { it.degrees < beliefs.target - TOLERANCE && beliefs.mode != Mode.HEATING }
        } triggers {
            agent.print("It's ${context.degrees}°C, heating up to ${agent.beliefs.target}°C")
            hvac.heat()
        }
        adding.belief {
            this as? Temperature
        } onlyWhen {
            context.takeIf { it.degrees > beliefs.target + TOLERANCE && beliefs.mode != Mode.COOLING }
        } triggers {
            agent.print("It's ${context.degrees}°C, cooling down to ${agent.beliefs.target}°C")
            hvac.cool()
        }
        adding.belief {
            this as? Temperature
        } onlyWhen {
            val reached = when (beliefs.mode) {
                Mode.HEATING -> context.degrees >= beliefs.target
                Mode.COOLING -> context.degrees <= beliefs.target
                Mode.OFF -> false
            }
            context.takeIf { reached }
        } triggers {
            agent.print("It's ${context.degrees}°C, switching off")
            hvac.off()
        }
    }
}

/**
 * Runs the thermostat in the [room], where time passes one step every [stepTime], until cancelled.
 */
suspend fun runThermostat(room: Room, stepTime: Duration): Unit = coroutineScope {
    val home = node(NodeBuilders.baseNode()) {
        withAgents(thermostat(Hvac(room)))
    }
    launch { room.simulate(home, stepTime) }
    mas(NodeBuilders.baseNode()) {
        withNodes(home)
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
