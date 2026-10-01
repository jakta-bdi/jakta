import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.VacuumWorldApp
import ui.VacuumWorldAppState

/**
 * Entry point of the Vacuum World desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Vacuum World",
        state = rememberWindowState(width = 1300.dp, height = 850.dp),
    ) {
        MaterialTheme {
            VacuumWorldApp(remember { VacuumWorldAppState() })
        }
    }
}
