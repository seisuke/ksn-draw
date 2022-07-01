package ksn.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import org.jetbrains.skia.Paint
import org.jetbrains.skia.impl.use

@Composable
fun Layer(
    width: Dp,
    height: Dp,
    mouseModifier: Modifier? = null,
    content: Canvas.(Paint) -> Unit
) {
    val modifier = mouseModifier ?: Modifier
    Canvas(
        modifier.width(width)
            .height(height)
    ) {
        Paint().use { paint ->
            drawIntoCanvas { canvas ->
                canvas.content(paint)
            }
        }
    }
}
