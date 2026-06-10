package ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp

val colorMap: Map<String, Color> = mapOf(
    "A" to Color(0xFF3F6BAA),
    "B" to Color(0xFF6BAA3F),
    "C" to Color(0xFFAA3F6B),
    "D" to Color(0xFF6B3FAA),
    "E" to Color(0xFFAA6B3F),
    "F" to Color(0xFF3FAA6B)
)

fun DrawScope.drawBlock(
    label: String,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    textMeasurer: TextMeasurer
) {
    // block shape
    drawRect(
        color = colorMap[label] ?: Color.Gray,
        topLeft = Offset(x, y),
        size = Size(width, height),
    )

    // measure text
    val textLayout = textMeasurer.measure(
        text = label,
        style = TextStyle(
            color = Color.White,
            fontSize = 20.sp
        )
    )

    // centered text
    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(
            x + width / 2 - textLayout.size.width / 2,
            y + height / 2 - textLayout.size.height / 2
        )
    )
}
