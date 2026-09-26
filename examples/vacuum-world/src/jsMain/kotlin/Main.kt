import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.VacuumWorldApp
import ui.VacuumWorldAppState

/**
 * Entry point of the Vacuum World web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            VacuumWorldApp(remember { VacuumWorldAppState() })
        }
    }
}
