import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.TicTacToeApp
import ui.TicTacToeAppState

/**
 * Entry point of the Tic-Tac-Toe web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            TicTacToeApp(remember { TicTacToeAppState() })
        }
    }
}
