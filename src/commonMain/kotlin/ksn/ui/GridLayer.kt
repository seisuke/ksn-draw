package ksn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import ksn.Constants
import org.jetbrains.skia.Paint

@Composable
fun GridLayer(width: Dp, scale: Float) {

    Layer(
        width * scale,
        width * 2 * scale,
    ) { paint ->
        paint.color = 0xFF000000.toInt()
        drawGrid(nativeCanvas, paint, width.value, scale)
    }
}

private fun drawGrid(
    nativeCanvas: NativeCanvas,
    paint: Paint,
    size: Float,
    scale: Float
) {
    val points = (0..size.toInt()).step(Constants.GRID_WIDTH).asIterable().map(Int::toFloat)
    nativeCanvas.apply {
        scale(scale, scale)
        points.forEach { it ->
            drawLine(it, 0f, it, size * 2, paint)
            drawLine(0f, it * 2, size, it * 2, paint)
        }
    }
}
