@file:Suppress("LongMethod") // example code: DSL definitions read best in one place

package ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt
import kotlin.time.Duration
import model.Block
import model.Move
import model.Stacks

private const val GROUND_HEIGHT = 60f
private const val TOP_MARGIN = 20f
private const val MAX_BLOCK_SIZE = 90f
private const val BLOCK_TO_SPACING = 0.9f

private const val RAIL_HEIGHT = 8f
private const val CARRIAGE_HEIGHT = 18f
private const val MIN_CABLE = 12f
private const val GRIPPER_HEIGHT = 8f

// the share of the move delay the arm uses, so that it is back up before the next move starts
private const val ARM_SHARE_OF_DELAY = 0.9

private val RAIL_COLOR = Color(0xFF4A4A4A)
private val ARM_COLOR = Color(0xFFE0A100)
private val CABLE_COLOR = Color(0xFF6D6D6D)

/**
 * Keeps each stack in the column it started in, identified by its bottom block, which never moves while the stack
 * has other blocks: new stacks take the leftmost free column.
 */
private fun stableColumns(stacks: Stacks, previous: Map<Block, Int>, columnCount: Int): Map<Block, Int> {
    val bottoms = stacks.map { it.first() }
    val kept = previous.filter { (bottom, column) -> bottom in bottoms && column < columnCount }
    val free = (0 until columnCount).filterNot { it in kept.values }.iterator()
    return bottoms.associateWith { kept[it] ?: free.next() }
}

/**
 * The leftmost column without a stack, where a block moved to the table lands.
 */
private fun firstFreeColumn(columns: Map<Block, Int>, columnCount: Int): Int =
    (0 until columnCount).firstOrNull { it !in columns.values } ?: (columnCount - 1)

/**
 * Where blocks and the arm are drawn: one column per block, so that every stack keeps its place.
 */
private class PlaneLayout(size: Size, val columnCount: Int, tallestStack: Float, withArm: Boolean) {
    val spacing = size.width / (columnCount + 1)
    val groundY = size.height - GROUND_HEIGHT
    val railY = TOP_MARGIN

    // the gripper's lowest point when the arm is up, with room above the stacks for a carried block
    val armUpY = railY + RAIL_HEIGHT + CARRIAGE_HEIGHT + MIN_CABLE + GRIPPER_HEIGHT
    val blockSize = minOf(
        MAX_BLOCK_SIZE,
        spacing * BLOCK_TO_SPACING,
        if (withArm) (groundY - armUpY) / (tallestStack + 1) else (groundY - TOP_MARGIN) / tallestStack,
    )

    fun columnX(column: Int) = (column + 1) * spacing

    fun blockRect(column: Int, level: Int) = Rect(
        Offset(columnX(column) - blockSize / 2, groundY - (level + 1) * blockSize),
        Size(blockSize, blockSize),
    )

    fun columnAt(x: Float): Int = ((x / spacing).roundToInt() - 1).coerceIn(0, columnCount - 1)
}

/**
 * Where the arm is at a given moment of a move, and whether it holds the block.
 */
private data class ArmPose(val x: Float, val gripperY: Float, val holding: Boolean, val placed: Boolean)

/**
 * The path of the arm for one move: from where it rests ([fromX]) to the block to [pick], then to where it will [drop]
 * it, going up to [upY] in between.
 */
private class ArmPath(val fromX: Float, val pick: Rect, val drop: Rect, val upY: Float) {
    /**
     * The arm slides over the block, lowers, grabs it, lifts it, slides over the destination, lowers, releases it
     * and lifts again: [progress] goes from 0 to 1 over the move.
     */
    @Suppress("MagicNumber")
    fun poseAt(progress: Float): ArmPose {
        fun phase(start: Float, end: Float) = ((progress - start) / (end - start)).coerceIn(0f, 1f)
        fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
        val sourceX = pick.center.x
        val pickY = pick.top
        val destX = drop.center.x
        val dropY = drop.top
        return when {
            progress < 0.20f -> ArmPose(lerp(fromX, sourceX, phase(0f, 0.20f)), upY, holding = false, placed = false)
            progress < 0.35f -> ArmPose(sourceX, lerp(upY, pickY, phase(0.20f, 0.35f)), holding = false, placed = false)
            progress < 0.50f -> ArmPose(sourceX, lerp(pickY, upY, phase(0.35f, 0.50f)), holding = true, placed = false)
            progress < 0.70f -> ArmPose(lerp(sourceX, destX, phase(0.50f, 0.70f)), upY, holding = true, placed = false)
            progress < 0.85f -> ArmPose(destX, lerp(upY, dropY, phase(0.70f, 0.85f)), holding = true, placed = false)
            else -> ArmPose(destX, lerp(dropY, upY, phase(0.85f, 1f)), holding = false, placed = true)
        }
    }
}

/**
 * Whether [stacks] already show this move done: the world state and the move in progress arrive separately,
 * so for a moment the new state can be seen while the move still looks in progress.
 */
private fun Move.isDoneIn(stacks: Stacks): Boolean {
    val stack = stacks.firstOrNull { block in it } ?: return true
    return if (destination == null) stack.size == 1 else stack.size >= 2 && stack[stack.lastIndex - 1] == destination
}

/**
 * Draws the rail on the ceiling, the carriage sliding on it, the cable and the gripper, whose bottom is at [gripperY].
 */
@Suppress("MagicNumber")
private fun DrawScope.drawArm(layout: PlaneLayout, x: Float, gripperY: Float, holding: Boolean) {
    drawRect(RAIL_COLOR, Offset(0f, layout.railY), Size(size.width, RAIL_HEIGHT))
    val carriageWidth = layout.blockSize * 0.7f
    val carriageTop = layout.railY + RAIL_HEIGHT
    drawRect(ARM_COLOR, Offset(x - carriageWidth / 2, carriageTop), Size(carriageWidth, CARRIAGE_HEIGHT))
    val gripperTop = gripperY - GRIPPER_HEIGHT
    drawLine(CABLE_COLOR, Offset(x, carriageTop + CARRIAGE_HEIGHT), Offset(x, gripperTop), strokeWidth = 4f)
    // the gripper: a bar with a finger on each side, closed around a held block, open otherwise
    val gripperWidth = layout.blockSize * if (holding) 1.1f else 1.3f
    drawRect(ARM_COLOR, Offset(x - gripperWidth / 2, gripperTop), Size(gripperWidth, GRIPPER_HEIGHT))
    val fingerLength = layout.blockSize * 0.35f
    listOf(x - gripperWidth / 2, x + gripperWidth / 2 - 6f).forEach { fingerX ->
        drawRect(ARM_COLOR, Offset(fingerX, gripperY), Size(6f, fingerLength))
    }
}

/**
 * Renders stacks of blocks; when [onMove] is given, the top block of a stack can be dragged
 * onto another stack or onto a free spot of the table.
 * With [withArm], a robot arm on the ceiling performs each [move] of the agent over [moveDuration].
 */
@Suppress("MagicNumber")
@Composable
fun BlocksWorldPlane(
    stacks: Stacks,
    onMove: ((Block, Block?) -> Unit)?,
    modifier: Modifier = Modifier,
    background: Color = Color(0xFFE9E2D0),
    withArm: Boolean = false,
    move: Move? = null,
    moveDuration: Duration = Duration.ZERO,
) {
    val textMeasurer = rememberTextMeasurer()
    var dragged by remember { mutableStateOf<Block?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    val columnCount = stacks.sumOf { it.size }.coerceAtLeast(1)
    val columnMemory = remember { mutableMapOf<Block, Int>() }
    val columns = remember(stacks) {
        stableColumns(stacks, columnMemory, columnCount).also {
            columnMemory.clear()
            columnMemory.putAll(it)
        }
    }
    fun stackIn(column: Int) = stacks.firstOrNull { columns[it.first()] == column }

    // the arm rests where it left its last block; armFrom is where the current move started
    var armRestColumn by remember { mutableStateOf<Int?>(null) }
    var armFromColumn by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(1f) }
    val activeMove = move?.takeUnless { it.isDoneIn(stacks) }
    val sourceStack = activeMove?.let { m -> stacks.firstOrNull { m.block in it } }
    val destinationStack = activeMove?.destination?.let { d -> stacks.firstOrNull { d in it } }
    val destinationColumn = when {
        activeMove == null || sourceStack == null -> null

        activeMove.destination != null -> destinationStack?.let { columns[it.first()] }

        sourceStack.size == 1 -> columns[sourceStack.first()]

        // already on the table: put back where it is
        else -> firstFreeColumn(columns, columnCount)
    }
    LaunchedEffect(move) {
        if (move != null) {
            armFromColumn = armRestColumn
            progress.snapTo(0f)
            progress.animateTo(
                1f,
                tween((moveDuration.inWholeMilliseconds * ARM_SHARE_OF_DELAY).toInt(), easing = LinearEasing),
            )
            destinationColumn?.let { armRestColumn = it }
        }
    }

    val dragModifier = if (onMove == null) {
        Modifier
    } else {
        Modifier.pointerInput(stacks, onMove) {
            fun layout() = PlaneLayout(size.toSize(), columnCount, stacks.maxOf { it.size }.toFloat(), withArm)
            detectDragGestures(
                onDragStart = { start ->
                    // grabbing anywhere in a column picks its top block: start is reported past the touch slop
                    dragged = stackIn(layout().columnAt(start.x))?.last()
                    dragPosition = start
                },
                onDrag = { change, amount ->
                    change.consume()
                    dragPosition += amount
                },
                onDragEnd = {
                    dragged?.let { block ->
                        val destination = stackIn(layout().columnAt(dragPosition.x))?.last()
                        if (destination != block) onMove(block, destination)
                    }
                    dragged = null
                },
                onDragCancel = { dragged = null },
            )
        }
    }

    // while the arm carries a block, leave room for the stack it will land on; blocks shrink and grow smoothly
    val landingHeight = destinationStack?.size?.plus(1) ?: 0
    val tallestStack by animateFloatAsState(maxOf(stacks.maxOf { it.size }, landingHeight).toFloat())

    Canvas(modifier = modifier.background(background).fillMaxSize().then(dragModifier)) {
        val layout = PlaneLayout(size, columnCount, tallestStack, withArm)

        drawRect(Color.DarkGray, Offset(0f, layout.groundY), Size(size.width, GROUND_HEIGHT))

        if (onMove != null && dragged != null) {
            val target = layout.columnAt(dragPosition.x)
            for (column in 0 until columnCount) {
                if (stackIn(column) != null) continue
                val spot = layout.blockRect(column, 0)
                drawRect(
                    color = if (column == target) Color(0xFF3F51AA) else Color.Gray,
                    topLeft = spot.topLeft,
                    size = spot.size,
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
                )
            }
            stackIn(target)?.let { stack ->
                val top = layout.blockRect(target, stack.size)
                drawRect(Color(0x553F51AA), top.topLeft, top.size)
            }
        }

        stacks.forEach { stack ->
            val column = columns.getValue(stack.first())
            stack.forEachIndexed { level, block ->
                if (block != dragged && block != activeMove?.block) {
                    drawBlock(block.id, layout.blockRect(column, level), textMeasurer)
                }
            }
        }

        dragged?.let {
            drawBlock(it.id, Rect(center = dragPosition, radius = layout.blockSize / 2), textMeasurer)
        }

        if (withArm) {
            val restX = layout.columnX(armRestColumn ?: (columnCount - 1) / 2)
            if (activeMove == null || sourceStack == null || destinationColumn == null) {
                drawArm(layout, restX, layout.armUpY, holding = false)
            } else {
                val pick = layout.blockRect(columns.getValue(sourceStack.first()), sourceStack.lastIndex)
                val drop = layout.blockRect(destinationColumn, destinationStack?.size ?: 0)
                val path = ArmPath(armFromColumn?.let(layout::columnX) ?: restX, pick, drop, layout.armUpY)
                val pose = path.poseAt(progress.value)
                val carried =
                    Rect(Offset(pose.x - layout.blockSize / 2, pose.gripperY), Size(layout.blockSize, layout.blockSize))
                drawBlock(
                    activeMove.block.id,
                    when {
                        pose.placed -> drop
                        pose.holding -> carried
                        else -> pick
                    },
                    textMeasurer,
                )
                drawArm(layout, pose.x, pose.gripperY, pose.holding)
            }
        }
    }
}
