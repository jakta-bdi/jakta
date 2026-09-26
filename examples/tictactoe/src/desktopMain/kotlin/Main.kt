import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.TicTacToeApp
import ui.TicTacToeAppState

/**
 * Entry point of the Tic-Tac-Toe desktop application.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tic-Tac-Toe",
        state = rememberWindowState(width = 1300.dp, height = 850.dp),
    ) {
        MaterialTheme {
            TicTacToeApp(remember { TicTacToeAppState() })
        }
    }
}
