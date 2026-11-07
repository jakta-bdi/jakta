package ui

import Player
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import model.BoardState
import model.Mark

private const val MAX_THINK_TIME_MS = 2000f
private const val TRACE_SHARE_COMPACT = 0.3f
private val TIGHT_PADDING = PaddingValues(horizontal = 10.dp, vertical = 8.dp)

/**
 * The main Composable function for the Tic-Tac-Toe application.
 */
@Composable
fun TicTacToeApp(app: TicTacToeAppState) {
    val scope = rememberCoroutineScope()
    val board by app.board.state.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < COMPACT_WIDTH
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(app, onNewGame = { app.newGame(scope) })
            Divider()
            if (compact) {
                // on a phone: the board, then the trace below it
                Game(app, board, modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp))
                AgentTracePanel(modifier = Modifier.fillMaxWidth().height(maxHeight * TRACE_SHARE_COMPACT))
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    Game(app, board, modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp))
                    AgentTracePanel(modifier = Modifier.width(sideTraceWidth(maxWidth)).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun Toolbar(app: TicTacToeAppState, onNewGame: () -> Unit) {
    // the controls wrap on more lines when the window is narrow, each group staying together
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ToolGroup {
            Button(onClick = onNewGame) { Text("New game") }
            for (mark in Mark.entries) {
                OutlinedButton(onClick = { app.togglePlayer(mark) }) {
                    Text("$mark: ${if (app.players[mark] == Player.HUMAN) "you" else "agent"}")
                }
            }
        }
        ToolGroup {
            Text("Difficulty")
            for (difficulty in Difficulty.entries) {
                OutlinedButton(
                    onClick = { app.difficulty = difficulty },
                    enabled = difficulty != app.difficulty,
                    contentPadding = TIGHT_PADDING,
                ) {
                    Text(difficulty.name.lowercase())
                }
            }
        }
        ToolGroup {
            Text("${BoardState(app.size).winLength} in a row")
            for (size in BOARD_SIZES) {
                val selected = size == app.size
                OutlinedButton(
                    onClick = { app.changeSize(size) },
                    enabled = !selected,
                    contentPadding = TIGHT_PADDING,
                ) {
                    Text("$size×$size")
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

@Composable
private fun Game(app: TicTacToeAppState, board: BoardState, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(status(board, app), style = MaterialTheme.typography.h5)
        BoardView(board, onClick = app::click, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Agents think: ${app.thinkTime.inWholeMilliseconds} ms")
            Slider(
                value = app.thinkTime.inWholeMilliseconds.toFloat(),
                onValueChange = { app.thinkTime = it.roundToInt().milliseconds },
                valueRange = 0f..MAX_THINK_TIME_MS,
                modifier = Modifier.widthIn(max = 240.dp).padding(start = 12.dp),
            )
        }
    }
}

private fun status(board: BoardState, app: TicTacToeAppState): String {
    val winner = board.winner
    val turn = board.turn
    return when {
        winner != null -> "$winner wins!"
        board.isOver -> "It's a draw"
        !app.isRunning -> "Press New game to start"
        app.players[turn] == Player.HUMAN -> "Your move ($turn)"
        else -> "$turn is thinking…"
    }
}

/**
 * The grid of cells, as large as fits, highlighting the winning line.
 */
@Suppress("MagicNumber")
@Composable
private fun BoardView(board: BoardState, onClick: (Int, Int) -> Unit, modifier: Modifier = Modifier) {
    val winning = board.winningLine.orEmpty().toSet()
    BoxWithConstraints(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        val cellSize = minOf(maxWidth, maxHeight) / board.size
        Column {
            for (y in 0 until board.size) {
                Row {
                    for (x in 0 until board.size) {
                        val mark = board[x, y]
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(if ((x to y) in winning) Color(0xFFFFE082) else Color.White)
                                .border(2.dp, Color.DarkGray)
                                .clickable { onClick(x, y) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (mark != null) {
                                Text(
                                    mark.name,
                                    fontSize = (cellSize.value * 0.5f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mark == Mark.X) Color(0xFF3F51AA) else Color(0xFFAA3F3F),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
