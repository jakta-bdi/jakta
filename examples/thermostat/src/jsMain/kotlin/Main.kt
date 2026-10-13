import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.ThermostatApp

/**
 * Entry point of the Thermostat web application.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme {
            ThermostatApp()
        }
    }
}
