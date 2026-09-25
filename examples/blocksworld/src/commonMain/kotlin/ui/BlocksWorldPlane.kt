package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt
import model.Block
import model.Stacks

private const val GROUND_HEIGHT = 60f
private const val TOP_MARGIN = 20f
private const val MAX_BLOCK_SIZE = 90f
private const val BLOCK_TO_SPACING = 0.9f

/**
 * Where stacks and blocks are drawn: one slot per stack plus an empty slot on the table.
 */
private class PlaneLayout(size: Size, stacks: Stacks) {
    val slots = stacks.size + 1
    val spacing = size.width / (slots + 1)
    val groundY = size.height - GROUND_HEIGHT
    val blockSize = minOf(
        MAX_BLOCK_SIZE,
        spacing * BLOCK_TO_SPACING,
        (groundY - TOP_MARGIN) / (stacks.maxOfOrNull { it.size } ?: 1),
    )

    fun blockRect(slot: Int, level: Int) = Rect(
        Offset((slot + 1) * spacing - blockSize / 2, groundY - (level + 1) * blockSize),
        Size(blockSize, blockSize),
    )

    fun slotAt(x: Float): Int = ((x / spacing).roundToInt() - 1).coerceIn(0, slots - 1)
}

/**
 * Renders stacks of blocks; when [onMove] is given, the top block of a stack can be dragged
 * onto another stack or onto the empty spot of the table.
 */
@Suppress("MagicNumber")
@Composable
fun BlocksWorldPlane(stacks: Stacks, onMove: ((Block, Block?) -> Unit)?, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    var dragged by remember { mutableStateOf<Block?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    val dragModifier = if (onMove == null) {
        Modifier
    } else {
        Modifier.pointerInput(stacks, onMove) {
            detectDragGestures(
                onDragStart = { start ->
                    // grabbing anywhere in a column picks its top block: start is reported past the touch slop
                    dragged = stacks.getOrNull(PlaneLayout(size.toSize(), stacks).slotAt(start.x))?.last()
                    dragPosition = start
                },
                onDrag = { change, amount ->
                    change.consume()
                    dragPosition += amount
                },
                onDragEnd = {
                    dragged?.let { block ->
                        val slot = PlaneLayout(size.toSize(), stacks).slotAt(dragPosition.x)
                        val destination = stacks.getOrNull(slot)?.last()
                        if (destination != block) onMove(block, destination)
                    }
                    dragged = null
                },
                onDragCancel = { dragged = null },
            )
        }
    }

    Canvas(modifier = modifier.background(Color(0xFFE9E2D0)).fillMaxSize().then(dragModifier)) {
        val layout = PlaneLayout(size, stacks)

        drawRect(Color.DarkGray, Offset(0f, layout.groundY), Size(size.width, GROUND_HEIGHT))

        if (onMove != null) {
            val targetSlot = dragged?.let { layout.slotAt(dragPosition.x) }
            val tableSpot = layout.blockRect(stacks.size, 0)
            drawRect(
                color = if (targetSlot == stacks.size) Color(0xFF3F51AA) else Color.Gray,
                topLeft = tableSpot.topLeft,
                size = tableSpot.size,
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
            )
            if (targetSlot != null && targetSlot < stacks.size) {
                val top = layout.blockRect(targetSlot, stacks[targetSlot].size)
                drawRect(Color(0x553F51AA), top.topLeft, top.size)
            }
        }

        stacks.forEachIndexed { i, stack ->
            stack.forEachIndexed { level, block ->
                if (block != dragged) {
                    val rect = layout.blockRect(i, level)
                    drawBlock(block.id, rect.left, rect.top, rect.width, rect.height, textMeasurer)
                }
            }
        }

        dragged?.let {
            val half = layout.blockSize / 2
            drawBlock(
                it.id,
                dragPosition.x - half,
                dragPosition.y - half,
                layout.blockSize,
                layout.blockSize,
                textMeasurer,
            )
        }
    }
}
