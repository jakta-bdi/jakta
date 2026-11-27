import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.KitchenApp
import ui.KitchenAppState

/**
 * Entry point of the Kitchen web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            KitchenApp(remember { KitchenAppState() })
        }
    }
}
