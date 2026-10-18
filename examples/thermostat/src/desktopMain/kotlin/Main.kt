import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.ThermostatApp

/**
 * Entry point of the Thermostat desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Thermostat",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
    ) {
        MaterialTheme {
            ThermostatApp()
        }
    }
}
