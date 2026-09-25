import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.CleaningRobotsApp
import ui.CleaningRobotsAppState

/**
 * Entry point of the Cleaning Robots web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            CleaningRobotsApp(remember { CleaningRobotsAppState() })
        }
    }
}
