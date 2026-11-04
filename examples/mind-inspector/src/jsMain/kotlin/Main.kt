import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.inspector.MindInspector
import it.unibo.jakta.inspector.MindInspectorView
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Runs the workshop and shows the mind inspector on it in the page.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Logger.setMinSeverity(Severity.Assert)
    val inspector = MindInspector()
    MainScope().launch { workshop().run(inspector.runner()) }
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme { MindInspectorView(inspector) }
    }
}
