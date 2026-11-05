package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MAX_TRACE_LINES = 500

/**
 * Collects what agents print (JaKtA logs `print` at [Severity.Assert]) so that the UI can show their reasoning.
 */
object AgentTrace : LogWriter() {
    private val mutableLines = MutableStateFlow<List<String>>(emptyList())

    /**
     * The most recent lines printed by the agents, oldest first.
     */
    val lines: StateFlow<List<String>> = mutableLines.asStateFlow()

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity == Severity.Assert

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        mutableLines.update { (it + "[$tag] $message").takeLast(MAX_TRACE_LINES) }
    }

    /**
     * Forgets all the collected lines.
     */
    fun clear() {
        mutableLines.value = emptyList()
    }

    /**
     * Routes what agents print to this trace (and the console), silencing JaKtA's internal logging.
     */
    fun install() {
        Logger.setMinSeverity(Severity.Error)
        Logger.setLogWriters(platformLogWriter(), this)
    }
}

/**
 * Shows what the agents printed, following the newest line.
 */
@Suppress("MagicNumber")
@Composable
fun AgentTracePanel(modifier: Modifier = Modifier) {
    val lines by AgentTrace.lines.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
    }

    Column(modifier = modifier.background(Color(0xFF1E1E1E)).padding(8.dp)) {
        Text("Agent trace", color = Color.White, style = MaterialTheme.typography.subtitle2)
        LazyColumn(state = listState) {
            items(lines) { line ->
                Text(line, color = Color(0xFFD4D4D4), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}
