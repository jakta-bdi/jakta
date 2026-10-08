package ui

import Mode
import Room
import RoomState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import runThermostat

private val STEP_TIME = 200.milliseconds

/**
 * The thermostat application: a room whose temperature, target and outside temperature can be changed
 * with sliders while the thermostat agent keeps it comfortable.
 *
 * @param agentDispatcher where the thermostat runs, off the UI thread on desktop.
 */
@Suppress("MagicNumber")
@Composable
fun ThermostatApp(agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {
    val room = remember { Room(RoomState(temperature = 15.0, target = 21.0, outside = 8.0)) }
    val state by room.state.collectAsState()

    LaunchedEffect(room) {
        AgentTrace.install()
        withContext(agentDispatcher) { runThermostat(room, STEP_TIME) }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("${state.temperature.oneDecimal()} °C", fontSize = 64.sp)
            Text(state.mode.label, color = state.mode.color, style = MaterialTheme.typography.h5)
            Text("Drag the sliders while the thermostat works: it reacts to whatever happens in the room.")
            Setting("Room temperature", state.temperature, 0f..40f, room::setTemperature)
            Setting("Target", state.target, 10f..30f) { room.setTarget(it.roundToHalf()) }
            Setting("Outside", state.outside, -10f..40f, room::setOutside)
        }
        AgentTracePanel(modifier = Modifier.width(420.dp).fillMaxHeight())
    }
}

@Composable
private fun Setting(label: String, value: Double, range: ClosedFloatingPointRange<Float>, onChange: (Double) -> Unit) {
    Column {
        Text("$label: ${value.oneDecimal()} °C")
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toDouble()) },
            valueRange = range,
            modifier = Modifier.width(360.dp),
        )
    }
}

// formatted by hand: Kotlin/JS would print 21.0 as "21"
private fun Double.oneDecimal(): String {
    val tenths = (this * 10).roundToInt()
    val sign = if (tenths < 0) "-" else ""
    return "$sign${abs(tenths) / 10}.${abs(tenths) % 10}"
}

private fun Double.roundToHalf() = (this * 2).roundToInt() / 2.0

private val Mode.label get() = when (this) {
    Mode.OFF -> "Idle"
    Mode.HEATING -> "Heating"
    Mode.COOLING -> "Cooling"
}

@Suppress("MagicNumber")
private val Mode.color get() = when (this) {
    Mode.OFF -> Color.Gray
    Mode.HEATING -> Color(0xFFE65100)
    Mode.COOLING -> Color(0xFF1565C0)
}
