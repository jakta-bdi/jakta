import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class ThermostatTest {
    @Test
    fun thermostatHeatsAColdRoomAndCoolsAHotOne() = runTest {
        Logger.setMinSeverity(Severity.Assert)
        val room = Room(RoomState(temperature = 15.0, target = 21.0, outside = 8.0))
        val thermostat = launch { runThermostat(room, stepTime = 10.milliseconds) }
        room.state.first { it.mode == Mode.HEATING }
        val reached = room.state.first { it.mode == Mode.OFF }
        // the thermostat perceives temperatures rounded to tenths of a degree
        assertTrue(reached.temperature >= reached.target - 0.05, "Switched off at ${reached.temperature}°C")
        room.setTemperature(30.0)
        assertEquals(Mode.COOLING, room.state.first { it.mode != Mode.OFF }.mode)
        thermostat.cancel()
    }
}
