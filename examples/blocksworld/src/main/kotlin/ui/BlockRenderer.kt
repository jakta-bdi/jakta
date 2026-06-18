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
    "A" to Color(0xFFAA3F3F),
    "B" to Color(0xFF3F51AA),
    "C" to Color(0xFF68AA3F),
    "D" to Color(0xFFAA983F),
    "E" to Color(0xFFAA643F),
    "F" to Color(0xFF3FAA85),
    "G" to Color(0xFFAA3FAA),
    "H" to Color(0xFF5C3FAA),
    "I" to Color(0xFF6BAA89),
    "J" to Color(0xFF6B6BAA),
    "K" to Color(0xFF9C6BAA),
    "L" to Color(0xFFAA6B6B),
    "M" to Color(0xFF3F6B6B),
    "N" to Color(0xFF85AA3F),
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
