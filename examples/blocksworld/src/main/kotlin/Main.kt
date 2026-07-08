import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import ui.BlocksWorldApp
import ui.BlocksWorldAppState

/**
 * Entry point of the Blocks World application.
 */
fun main() = application {
    Logger.setMinSeverity(Severity.Assert)

    val appState = remember { BlocksWorldAppState() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Blocks World",
        state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
        ),
    ) {
        MaterialTheme {
            BlocksWorldApp(appState)
        }
    }
}
