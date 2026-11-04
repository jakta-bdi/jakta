import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.inspector.MindInspector
import it.unibo.jakta.inspector.MindInspectorWindow
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Runs the workshop twice, with different random choices, and opens the mind inspector on both runs, one tab each.
 */
@Suppress("InjectDispatcher") // the agents run off the UI thread
fun main() {
    Logger.setMinSeverity(Severity.Assert)
    val runs = arrayOf(MindInspector("seed 1"), MindInspector("seed 2"))
    runs.forEachIndexed { seed, run ->
        CoroutineScope(Dispatchers.Default).launch { workshop(Random(seed + 1)).run(run.runner()) }
    }
    application {
        MindInspectorWindow(*runs, onCloseRequest = ::exitApplication)
    }
}
