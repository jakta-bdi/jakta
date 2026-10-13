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
 * What the thermostat believes about the room: its [temperature], the [target] to keep,
 * and the [mode] of the heating and cooling system.
 */
data class RoomBelief(val temperature: Double, val target: Double, val mode: Mode)

private fun Double.roundedToTenths() = (this * 10).roundToInt() / 10.0

/**
 * A thermostat keeping the room within [TOLERANCE] degrees of its target, heating or cooling it with [hvac].
 */
fun thermostat(hvac: Hvac) = agent<RoomBelief, String, Any>(BaseAgentID("thermostat")) {
    embodiedAs { Any() }
    believes {
        // no temperature read yet: NaN fails every comparison, so no plan applies until the first reading
        +RoomBelief(temperature = Double.NaN, target = 20.0, mode = Mode.OFF)
    }
    handlesPerceptionEvents { perception ->
        val room = beliefs.single()
        val updated = when (perception) {
            is TemperatureReading -> room.copy(temperature = perception.degrees.roundedToTenths())
            is TargetSet -> room.copy(target = perception.degrees)
            is ModeChanged -> room.copy(mode = perception.mode)
            else -> room
        }
        if (updated == room) null else AgentUpdate.Belief(setOf(updated), setOf(room))
    }
    hasPlanLibrary {
        adding.belief {
            this
        } onlyWhen {
            context.takeIf { it.temperature < it.target - TOLERANCE && it.mode != Mode.HEATING }
        } triggers {
            agent.print("It's ${context.temperature}°C, heating up to ${context.target}°C")
            hvac.heat()
        }
        adding.belief {
            this
        } onlyWhen {
            context.takeIf { it.temperature > it.target + TOLERANCE && it.mode != Mode.COOLING }
        } triggers {
            agent.print("It's ${context.temperature}°C, cooling down to ${context.target}°C")
            hvac.cool()
        }
        adding.belief {
            this
        } onlyWhen {
            val reached = when (context.mode) {
                Mode.HEATING -> context.temperature >= context.target
                Mode.COOLING -> context.temperature <= context.target
                Mode.OFF -> false
            }
            context.takeIf { reached }
        } triggers {
            agent.print("It's ${context.temperature}°C, switching off")
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
