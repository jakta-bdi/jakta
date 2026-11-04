import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.inspector.MindInspector
import it.unibo.jakta.inspector.MindInspectorWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Runs the workshop and opens the mind inspector on it.
 */
@Suppress("InjectDispatcher") // the agents run off the UI thread
fun main() {
    Logger.setMinSeverity(Severity.Assert)
    val inspector = MindInspector()
    CoroutineScope(Dispatchers.Default).launch { workshop().run(inspector.runner()) }
    application {
        MindInspectorWindow(inspector, onCloseRequest = ::exitApplication)
    }
}
