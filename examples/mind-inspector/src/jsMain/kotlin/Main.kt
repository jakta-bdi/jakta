import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.inspector.MindInspector
import it.unibo.jakta.inspector.MindInspectorView
import kotlin.random.Random
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Runs the workshop twice, with different random choices, and shows the mind inspector on both runs in the page,
 * one tab each.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Logger.setMinSeverity(Severity.Assert)
    val runs = arrayOf(MindInspector("seed 1"), MindInspector("seed 2"))
    runs.forEachIndexed { seed, run -> MainScope().launch { workshop(Random(seed + 1)).run(run.runner()) } }
    ComposeViewport(checkNotNull(document.body)) {
        MaterialTheme { MindInspectorView(*runs) }
    }
}
