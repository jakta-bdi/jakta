import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.node.Node
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val INSULATION_LOSS = 0.02
private const val POWER = 0.4

/**
 * What the heating and cooling system of the room is doing.
 *
 * @property power how many degrees it adds to the room at each step.
 */
enum class Mode(val power: Double) {
    /** Neither heating nor cooling. */
    OFF(0.0),

    /** Warming the room up. */
    HEATING(POWER),

    /** Cooling the room down. */
    COOLING(-POWER),
}

/**
 * The state of the room: its [temperature], the [target] set on the thermostat, the temperature [outside],
 * towards which the room drifts, and what the heating and cooling system is doing.
 *
 * @property temperature the temperature of the room.
 * @property target the temperature the thermostat should keep.
 * @property outside the temperature outside.
 * @property mode what the heating and cooling system is doing.
 */
data class RoomState(val temperature: Double, val target: Double, val outside: Double, val mode: Mode = Mode.OFF)

/**
 * A room with a heating and cooling system the thermostat controls.
 * Users can change its temperatures at any time.
 */
class Room(initial: RoomState) {
    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the room.
     */
    val state: StateFlow<RoomState> = mutableState.asStateFlow()

    /** Sets the temperature of the room, e.g. as if a window was opened. */
    fun setTemperature(degrees: Double) = mutableState.update { it.copy(temperature = degrees) }

    /** Sets the temperature the thermostat should keep. */
    fun setTarget(degrees: Double) = mutableState.update { it.copy(target = degrees) }

    /** Sets the temperature outside, e.g. as the day goes by. */
    fun setOutside(degrees: Double) = mutableState.update { it.copy(outside = degrees) }

    /** Switches the heating and cooling system to [mode]. */
    fun switch(mode: Mode) = mutableState.update { it.copy(mode = mode) }

    /** One step of time: the room loses heat towards the outside, and the system heats or cools it. */
    fun step() = mutableState.update {
        it.copy(temperature = it.temperature + (it.outside - it.temperature) * INSULATION_LOSS + it.mode.power)
    }
}

/**
 * What the thermostat perceives: the readings of its display.
 *
 * @property temperature the temperature of the room.
 * @property target the temperature to keep.
 * @property mode what the heating and cooling system is doing.
 */
data class RoomReading(val temperature: Double, val target: Double, val mode: Mode) : Perception

/**
 * Lets time pass in the [room], one step every [stepTime], and shows each new state to the agents of [node].
 */
suspend fun Room.simulate(node: Node<*>, stepTime: Duration) {
    while (true) {
        delay(stepTime)
        step()
        val state = state.value
        node.publishEvent(RoomReading(state.temperature, state.target, state.mode))
    }
}

/**
 * The thermostat's skill: switching the heating and cooling system of the [room].
 */
class Hvac(private val room: Room) {
    /** Starts heating. */
    fun heat() = room.switch(Mode.HEATING)

    /** Starts cooling. */
    fun cool() = room.switch(Mode.COOLING)

    /** Stops heating or cooling. */
    fun off() = room.switch(Mode.OFF)
}
