import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.BlocksWorldApp
import ui.BlocksWorldAppState

/**
 * Entry point of the Blocks World web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            BlocksWorldApp(remember { BlocksWorldAppState() })
        }
    }
}
