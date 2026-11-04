import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.KitchenApp
import ui.KitchenAppState

/**
 * Entry point of the Kitchen desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kitchen",
        state = rememberWindowState(width = 1500.dp, height = 900.dp),
    ) {
        MaterialTheme {
            KitchenApp(remember { KitchenAppState() })
        }
    }
}
