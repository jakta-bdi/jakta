import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.CleaningRobotsApp
import ui.CleaningRobotsAppState

/**
 * Entry point of the Cleaning Robots desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cleaning Robots",
        state = rememberWindowState(width = 1300.dp, height = 850.dp),
    ) {
        MaterialTheme {
            CleaningRobotsApp(remember { CleaningRobotsAppState() })
        }
    }
}
