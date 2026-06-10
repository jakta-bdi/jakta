package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import model.Block

@Composable
fun BlocksWorldPlane(state: List<List<Block>>) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .background(Color(0xFFE9E2D0))
            .fillMaxSize()
    ) {
        val stackCount = state.size.coerceAtLeast(1)

        val stackSpacing = size.width / (stackCount + 1)
        val blockW = 90f
        val blockH = 90f

        drawLine(
            Color.DarkGray,
            Offset(0f, size.height-50f),
            Offset(size.width, size.height-50f),
            100f
        )

        state.forEachIndexed { i, stack ->
            val xCenter = (i + 1) * stackSpacing

            stack.forEachIndexed { level, block ->
                val x = xCenter - blockW / 2
                val y = size.height - 100f - (level + 1) * blockH

                drawBlock(
                    label = block.id,
                    x = x,
                    y = y,
                    width = blockW,
                    height = blockH,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}
