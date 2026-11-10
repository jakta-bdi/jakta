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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
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
private const val SIDE_TRACE_SHARE = 0.3f

/** Below this width, as on a phone, the app stacks its panels one above the other, with the trace at the bottom. */
val COMPACT_WIDTH: Dp = 720.dp

/** How wide the trace is when it sits beside the rest of the app, in a window [width] wide. */
@Suppress("MagicNumber")
fun sideTraceWidth(width: Dp): Dp = (width * SIDE_TRACE_SHARE).coerceIn(240.dp, 420.dp)

/**
 * A line of the trace.
 *
 * @property agent the name of the agent that printed it.
 * @property message what the agent printed.
 */
data class TraceLine(val agent: String, val message: String)

/**
 * Collects what agents print (JaKtA logs `print` at [Severity.Assert]) so that the UI can show their reasoning.
 */
object AgentTrace : LogWriter() {
    private val mutableLines = MutableStateFlow<List<TraceLine>>(emptyList())

    /**
     * The most recent lines printed by the agents, oldest first.
     */
    val lines: StateFlow<List<TraceLine>> = mutableLines.asStateFlow()

    override fun isLoggable(tag: String, severity: Severity): Boolean = severity == Severity.Assert

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        mutableLines.update { (it + TraceLine(tag, message)).takeLast(MAX_TRACE_LINES) }
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

@Suppress("MagicNumber")
private val TRACE_BACKGROUND = Color(0xFFF5F6F8)

@Suppress("MagicNumber")
private val TRACE_TEXT = Color(0xFF37474F)

/** The colours agent names are written with, so that each agent's lines are easy to tell apart. */
@Suppress("MagicNumber")
private val AGENT_COLOURS = listOf(
    Color(0xFF3949AB),
    Color(0xFFC62828),
    Color(0xFF2E7D32),
    Color(0xFF8E24AA),
    Color(0xFFEF6C00),
    Color(0xFF00838F),
)

private fun agentColour(agent: String): Color = AGENT_COLOURS[agent.hashCode().mod(AGENT_COLOURS.size)]

/**
 * Shows what the agents printed, following the newest line.
 * Each agent's name is written in its colour from [agentColours], such as the colour the app draws it with,
 * or else in one picked from its name.
 */
@Suppress("MagicNumber")
@Composable
fun AgentTracePanel(modifier: Modifier = Modifier, agentColours: Map<String, Color> = emptyMap()) {
    val lines by AgentTrace.lines.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.lastIndex)
    }

    Column(modifier = modifier.background(TRACE_BACKGROUND).padding(8.dp)) {
        Text("Agent trace", color = TRACE_TEXT, style = MaterialTheme.typography.subtitle2)
        if (lines.isEmpty()) {
            Text(
                "What the agents print shows up here.",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
            )
        }
        LazyColumn(state = listState) {
            items(lines) { line ->
                val nameColour = agentColours[line.agent] ?: agentColour(line.agent)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = nameColour, fontWeight = FontWeight.Bold)) {
                            append(line.agent)
                        }
                        append("  ")
                        append(line.message)
                    },
                    color = TRACE_TEXT,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }
        }
    }
}
