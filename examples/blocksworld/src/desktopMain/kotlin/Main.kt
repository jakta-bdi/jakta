import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.BlocksWorldApp
import ui.BlocksWorldAppState

/**
 * Entry point of the Blocks World desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Blocks World",
        state = rememberWindowState(width = 1400.dp, height = 800.dp),
    ) {
        MaterialTheme {
            BlocksWorldApp(remember { BlocksWorldAppState() })
        }
    }
}
