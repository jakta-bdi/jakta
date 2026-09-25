package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_MOVE_DELAY_MS = 2000f

/**
 * The main Composable function for the Blocks World application.
 *
 * @param app The state of the Blocks World application.
 */
@Composable
fun BlocksWorldApp(app: BlocksWorldAppState) {
    val scope = rememberCoroutineScope()
    val worldState by app.world.state.collectAsState()
    val goalError = app.goalError

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = app.seed,
                    onValueChange = { app.seed = it },
                    label = { Text("Seed") },
                    modifier = Modifier.width(120.dp),
                )
                OutlinedTextField(
                    value = app.blockCount,
                    onValueChange = { app.blockCount = it },
                    label = { Text("Blocks") },
                    modifier = Modifier.width(120.dp),
                )
                OutlinedTextField(
                    value = app.goalText,
                    onValueChange = { app.goalText = it },
                    label = { Text(goalError ?: "Goal: towers listed top to bottom, separated by ;") },
                    isError = goalError != null,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = { app.play(scope) }, enabled = !app.isRunning && goalError == null) {
                    Text("Play")
                }
                Button(onClick = { app.stop() }, enabled = app.isRunning) {
                    Text("Stop")
                }
                Button(onClick = { app.reset() }) {
                    Text("Reset")
                }
                Text("Move delay: ${app.moveDelay.inWholeMilliseconds} ms", modifier = Modifier.padding(start = 16.dp))
                Slider(
                    value = app.moveDelay.inWholeMilliseconds.toFloat(),
                    onValueChange = { app.moveDelay = it.roundToInt().milliseconds },
                    valueRange = 0f..MAX_MOVE_DELAY_MS,
                    modifier = Modifier.width(240.dp),
                )
            }
        }

        Divider()

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(8.dp)) {
                BlocksWorldPlane(worldState)
            }
            AgentTracePanel(modifier = Modifier.width(420.dp).fillMaxHeight())
        }
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
