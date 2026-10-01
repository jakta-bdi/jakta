package ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import model.Content
import model.Direction
import model.Pos
import model.VacuumState

private const val MAX_STEP_TIME_MS = 1000f
private const val MAX_DUST_CHANCE = 0.3f
private const val PERCENT = 100

/**
 * The main Composable function for the Vacuum World application.
 */
@Composable
fun VacuumWorldApp(app: VacuumWorldAppState) {
    val scope = rememberCoroutineScope()
    val world by app.world.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (app.isRunning) {
                Button(onClick = { app.stop() }) { Text("Stop") }
            } else {
                Button(onClick = { app.start(scope) }) { Text("Start") }
            }
            OutlinedButton(onClick = { app.reset() }) { Text("Reset") }
            Text("Cleaned: ${world.cleaned}   Dust left: ${world.dust.size}", modifier = Modifier.width(220.dp))
            Text("Step: ${app.stepTime.inWholeMilliseconds} ms")
            Slider(
                value = app.stepTime.inWholeMilliseconds.toFloat(),
                onValueChange = { app.stepTime = it.roundToInt().milliseconds },
                valueRange = 0f..MAX_STEP_TIME_MS,
                modifier = Modifier.width(160.dp),
            )
            Text("New dust: ${(app.dustChance * PERCENT).roundToInt()}% per step")
            Slider(
                value = app.dustChance.toFloat(),
                onValueChange = { app.dustChance = it.toDouble() },
                valueRange = 0f..MAX_DUST_CHANCE,
                modifier = Modifier.width(160.dp),
            )
        }
        Text(
            "Click a cell to drop or remove dust, even while the robot works.",
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Divider(modifier = Modifier.padding(top = 8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp), contentAlignment = Alignment.Center) {
                WorldGrid(world, onClick = app::toggleDust)
            }
            AgentTracePanel(modifier = Modifier.width(420.dp).fillMaxHeight())
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun WorldGrid(world: VacuumState, onClick: (Pos) -> Unit) {
    BoxWithConstraints(modifier = Modifier.aspectRatio(world.width.toFloat() / world.height)) {
        val cellSize = minOf(maxWidth / world.width, maxHeight / world.height)
        Column {
            for (y in 0 until world.height) {
                Row {
                    for (x in 0 until world.width) {
                        val pos = Pos(x, y)
                        val content = world.contentAt(pos)
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(if (content == Content.OBSTACLE) Color(0xFF5D4037) else Color(0xFFF5F0E6))
                                .border(0.5.dp, Color(0xFFD7CCC8))
                                .clickable { onClick(pos) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (content == Content.DUST) Dust(cellSize * 0.35f)
                            if (pos == world.robot) Robot(world.facing, cellSize)
                        }
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun Dust(size: Dp) {
    Box(modifier = Modifier.size(size).background(Color(0xFF8D6E63), CircleShape))
}

@Suppress("MagicNumber")
@Composable
private fun Robot(facing: Direction, cellSize: Dp) {
    Canvas(modifier = Modifier.size(cellSize * 0.8f)) {
        drawCircle(Color(0xDD3F51AA))
        // a triangle pointing where the robot faces
        val r = size.minDimension / 2
        val tip = Offset(center.x + facing.dx * r * 0.7f, center.y + facing.dy * r * 0.7f)
        val side = Offset(-facing.dy * r * 0.4f, facing.dx * r * 0.4f)
        val back = Offset(center.x - facing.dx * r * 0.2f, center.y - facing.dy * r * 0.2f)
        val triangle = Path().apply {
            moveTo(tip.x, tip.y)
            lineTo(back.x + side.x, back.y + side.y)
            lineTo(back.x - side.x, back.y - side.y)
            close()
        }
        drawPath(triangle, Color.White)
    }
}
