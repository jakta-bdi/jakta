package ui

import RobotColour
import RobotState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import model.Direction
import model.Pos
import model.WorldState

private const val MIN_SPEED = 0.5f
private const val MAX_SPEED = 5f
private const val MAX_DUST_CHANCE = 0.01f
private const val PERCENT = 100
private const val TENTHS = 10.0
private const val TRACE_SHARE_COMPACT = 0.3f

/**
 * The main Composable function for the Vacuum World application.
 */
@Suppress("LongMethod")
@Composable
fun VacuumWorldApp(app: VacuumWorldAppState) {
    val scope = rememberCoroutineScope()
    val world by app.world.state.collectAsState()
    val robots = app.robots.map { it.state.collectAsState() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < COMPACT_WIDTH
        Column(modifier = Modifier.fillMaxSize()) {
            // the controls wrap on more lines when the window is narrow, each group staying together
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ToolGroup {
                    if (app.isRunning) {
                        Button(onClick = { app.stop() }) { Text("Stop") }
                    } else {
                        Button(onClick = { app.start(scope) }) { Text("Start") }
                    }
                    OutlinedButton(onClick = { app.reset() }) { Text("Reset") }
                    Text("Cleaned: ${robots.sumOf { it.value.cleaned }}   Dust left: ${world.dust.size}")
                }
                ToolGroup {
                    Text("Robots: ${app.robotCount}")
                    Slider(
                        value = app.robotCount.toFloat(),
                        onValueChange = {
                            if (it.roundToInt() != app.robotCount) app.changeRobotCount(it.roundToInt())
                        },
                        valueRange = 1f..MAX_ROBOTS.toFloat(),
                        steps = MAX_ROBOTS - 2,
                        enabled = !app.isRunning,
                        modifier = Modifier.width(100.dp),
                    )
                }
                ToolGroup {
                    Text("Speed: ${(app.speed * TENTHS).roundToInt() / TENTHS}x")
                    Slider(
                        value = app.speed.toFloat(),
                        onValueChange = { app.speed = it.toDouble() },
                        valueRange = MIN_SPEED..MAX_SPEED,
                        modifier = Modifier.width(120.dp),
                    )
                }
                ToolGroup {
                    Text("New dust: ${(app.dustChance * PERCENT * TENTHS).roundToInt() / TENTHS}% per cell per s")
                    Slider(
                        value = app.dustChance.toFloat(),
                        onValueChange = { app.dustChance = it.toDouble() },
                        valueRange = 0f..MAX_DUST_CHANCE,
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
            Text(
                "${if (compact) "Tap" else "Click"} a cell to drop or remove dust, even while the robots work.",
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            Divider(modifier = Modifier.padding(top = 8.dp))

            if (compact) {
                // on a phone: the world, then the trace below it
                Box(Modifier.weight(1f).fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    WorldGrid(world, robots.map { it.value }, onClick = app::toggleDust)
                }
                AgentTracePanel(modifier = Modifier.fillMaxWidth().height(maxHeight * TRACE_SHARE_COMPACT))
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight().padding(16.dp), contentAlignment = Alignment.Center) {
                        WorldGrid(world, robots.map { it.value }, onClick = app::toggleDust)
                    }
                    AgentTracePanel(modifier = Modifier.width(sideTraceWidth(maxWidth)).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun ToolGroup(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

@Suppress("MagicNumber")
@Composable
private fun WorldGrid(world: WorldState, robots: List<RobotState>, onClick: (Pos) -> Unit) {
    BoxWithConstraints(modifier = Modifier.aspectRatio(world.width.toFloat() / world.height)) {
        val cellSize = minOf(maxWidth / world.width, maxHeight / world.height)
        Column {
            for (y in 0 until world.height) {
                Row {
                    for (x in 0 until world.width) {
                        val pos = Pos(x, y)
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(if (world.isObstacle(pos)) Color(0xFF5D4037) else Color(0xFFF5F0E6))
                                .border(0.5.dp, Color(0xFFD7CCC8))
                                .clickable { onClick(pos) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (pos in world.dust) Dust(cellSize * 0.35f)
                            robots.zip(RobotColour.entries).forEach { (robot, colour) ->
                                if (pos == robot.position) Robot(robot.facing, Color(colour.argb), cellSize)
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
private fun Dust(size: Dp) {
    Box(modifier = Modifier.size(size).background(Color(0xFF8D6E63), CircleShape))
}

@Suppress("MagicNumber")
@Composable
private fun Robot(facing: Direction, colour: Color, cellSize: Dp) {
    Canvas(modifier = Modifier.size(cellSize * 0.8f)) {
        drawCircle(colour)
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
