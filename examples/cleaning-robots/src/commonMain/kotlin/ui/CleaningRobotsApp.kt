package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import model.MarsState
import model.Pos

private const val MAX_STEP_TIME_MS = 1000f

/**
 * The main Composable function for the Cleaning Robots application.
 */
@Composable
fun CleaningRobotsApp(app: CleaningRobotsAppState) {
    val scope = rememberCoroutineScope()
    val mars by app.mars.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = { app.start(scope) }, enabled = !app.isRunning) { Text("Start") }
            OutlinedButton(onClick = { app.reset() }) { Text("New planet") }
            Text(
                when {
                    app.isRunning -> "r1 is checking every slot; click cells to drop more garbage"
                    mars.garbage.isEmpty() -> "Mars is clean!"
                    else -> "Click cells to add or remove garbage, then press Start"
                },
                modifier = Modifier.width(460.dp),
            )
            Text("Step: ${app.stepTime.inWholeMilliseconds} ms")
            Slider(
                value = app.stepTime.inWholeMilliseconds.toFloat(),
                onValueChange = { app.stepTime = it.roundToInt().milliseconds },
                valueRange = 0f..MAX_STEP_TIME_MS,
                modifier = Modifier.width(200.dp),
            )
        }

        Divider()

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp), contentAlignment = Alignment.Center) {
                MarsGrid(mars, onClick = app::toggleGarbage)
            }
            AgentTracePanel(modifier = Modifier.width(420.dp).fillMaxHeight())
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun MarsGrid(mars: MarsState, onClick: (Pos) -> Unit) {
    BoxWithConstraints(modifier = Modifier.aspectRatio(1f)) {
        val cellSize = minOf(maxWidth, maxHeight) / mars.size
        Column {
            for (y in 0 until mars.size) {
                Row {
                    for (x in 0 until mars.size) {
                        val pos = Pos(x, y)
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(Color(0xFFD9A066))
                                .border(1.dp, Color(0xFF8F5B2E))
                                .clickable { onClick(pos) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (pos in mars.garbage) Garbage(cellSize * 0.3f)
                            if (pos == mars.r2) Robot("r2", Color(0xFFAA3F3F), cellSize)
                            if (pos == mars.r1) {
                                Robot("r1", Color(0xFF3F51AA), cellSize, carrying = mars.r1Carrying)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun Garbage(size: Dp) {
    Box(modifier = Modifier.size(size).background(Color(0xFF4E4E4E), CircleShape))
}

@Suppress("MagicNumber")
@Composable
private fun Robot(name: String, color: Color, cellSize: Dp, carrying: Boolean = false) {
    Box(
        modifier = Modifier.size(cellSize * 0.7f).background(color.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (carrying) "$name ●" else name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (cellSize.value * 0.2f).sp,
            style = MaterialTheme.typography.body1,
        )
    }
}
