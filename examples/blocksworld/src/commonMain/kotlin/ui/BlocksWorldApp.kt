@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import model.Move
import model.Stacks

private const val MAX_MOVE_DELAY_MS = 2000f
private const val WORLD_WEIGHT = 3f
private const val GOAL_WEIGHT = 2f
private const val TRACE_SHARE_COMPACT = 0.25f
private val GOAL_COLOR = Color(0xFF2E7D32)
private val GOAL_BACKGROUND = Color(0xFFE8F5E9)

/**
 * The main Composable function for the Blocks World application.
 *
 * @param app The state of the Blocks World application.
 */
@Composable
fun BlocksWorldApp(app: BlocksWorldAppState) {
    val scope = rememberCoroutineScope()
    val worldState by app.world.state.collectAsState()
    val currentMove by app.world.currentMove.collectAsState()
    val goalReached = worldState.sameTowersAs(app.goal)
    val editable = !app.isRunning

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < COMPACT_WIDTH
        // read here: the rows and columns below cannot see the constraints
        val traceHeight = maxHeight * TRACE_SHARE_COMPACT
        val traceWidth = sideTraceWidth(maxWidth)
        Column(modifier = Modifier.fillMaxSize()) {
            // the controls wrap on more lines when the window is narrow, each group staying together
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ToolGroup {
                    Button(onClick = { app.play(scope) }, enabled = editable && !goalReached) {
                        Text("Play")
                    }
                    Text("Blocks")
                    OutlinedButton(
                        onClick = { app.changeBlockCount(app.blockCount - 1) },
                        enabled = editable,
                    ) { Text("-") }
                    Text("${app.blockCount}")
                    OutlinedButton(
                        onClick = { app.changeBlockCount(app.blockCount + 1) },
                        enabled = editable,
                    ) { Text("+") }
                }
                ToolGroup {
                    Text("Move delay: ${app.moveDelay.inWholeMilliseconds} ms")
                    Slider(
                        value = app.moveDelay.inWholeMilliseconds.toFloat(),
                        onValueChange = { app.moveDelay = it.roundToInt().milliseconds },
                        valueRange = 0f..MAX_MOVE_DELAY_MS,
                        modifier = Modifier.width(160.dp),
                    )
                }
            }
            Text(
                when {
                    app.isRunning -> "The agent is working…"
                    goalReached -> "Goal reached!"
                    else -> "Drag blocks to arrange the world and the goal, then press Play"
                },
                modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 6.dp),
            )

            Divider()

            if (compact) {
                // on a phone: the goal and the world, then the trace below them
                Planes(app, worldState, currentMove, goalReached, Modifier.weight(1f).fillMaxWidth())
                AgentTracePanel(modifier = Modifier.fillMaxWidth().height(traceHeight))
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    Planes(app, worldState, currentMove, goalReached, Modifier.weight(1f).fillMaxHeight())
                    AgentTracePanel(modifier = Modifier.width(traceWidth).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun Planes(
    app: BlocksWorldAppState,
    worldState: Stacks,
    currentMove: Move?,
    goalReached: Boolean,
    modifier: Modifier,
) {
    val editable = !app.isRunning
    Column(modifier = modifier.padding(8.dp)) {
        // the goal comes first, framed, so that it reads as the state the world should reach
        Column(
            modifier = Modifier
                .weight(GOAL_WEIGHT)
                .border(3.dp, GOAL_COLOR, RoundedCornerShape(8.dp))
                .background(GOAL_BACKGROUND, RoundedCornerShape(8.dp))
                .padding(8.dp),
        ) {
            PanelHeader(
                title = if (goalReached) "Goal: reached!" else "Goal",
                subtitle = "the state the agent has to bring the world to",
                color = GOAL_COLOR,
            ) {
                OutlinedButton(onClick = { app.shuffleGoal() }, enabled = editable) { Text("Shuffle") }
            }
            BlocksWorldPlane(
                stacks = app.goal,
                onMove = if (editable) app::moveInGoal else null,
                background = GOAL_BACKGROUND,
            )
        }
        Column(modifier = Modifier.weight(WORLD_WEIGHT).padding(top = 8.dp)) {
            PanelHeader(title = "World", subtitle = "the current state, where the agent moves the blocks") {
                // shuffling the world also interrupts the agent
                OutlinedButton(onClick = { app.shuffleWorld() }) { Text("Shuffle") }
            }
            BlocksWorldPlane(
                stacks = worldState,
                onMove = if (editable) app::moveInWorld else null,
                withArm = true,
                move = currentMove,
                moveDuration = app.moveDelay,
            )
        }
    }
}

@Composable
private fun ToolGroup(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

@Composable
private fun PanelHeader(
    title: String,
    subtitle: String,
    color: Color = Color.Unspecified,
    actions: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.h6, color = color, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.body2,
            color = color,
            modifier = Modifier.weight(1f, fill = false),
        )
        actions()
    }
}
