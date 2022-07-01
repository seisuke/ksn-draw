package ksn.ui

import ksn.ModelElement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ksn.ascii.Ascii
import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.update.AppModel
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

@Composable
fun AsciiLayer(
    width: Dp,
    scale: Float,
    ascii: Ascii,
) {
    val element = ModelElement.current
    val shapes by element.mapAsState(AppModel::shapes)
    val typeface by element.mapAsState(AppModel::typeface)

    ascii.matrix.clear()
    shapes.forEach { shape ->
        when (shape) {
            is Rect -> ascii.apply {
                val partAscii = shape.toAsciiMatrix()
                matrix.merge(partAscii, shape.left, shape.top)
            }
            is Line -> ascii.apply {
                val partAscii = shape.toAsciiMatrix()
                matrix.merge(partAscii, shape.left, shape.top)
            }
        }
    }

    Layer(
        width * scale,
        width * 2 * scale,
    ) { paint ->
        paint.color = 0xFF0000FF.toInt()
        val loadedTypeface = typeface ?: return@Layer
        drawAscii(
            nativeCanvas,
            paint,
            loadedTypeface,
            20.dp.value,
            ascii,
            scale
        )
    }
}

private fun drawAscii(
    nativeCanvas: NativeCanvas,
    paint: Paint,
    typeFace: Typeface,
    height: Float,
    ascii: Ascii,
    scale: Float
) {
    val textLineList = ascii.render {
        it.value
    }.map { asciiLine ->
        TextLine.make(asciiLine, Font(typeFace, height))
    }

    nativeCanvas.apply {
        //clear(Color.TRANSPARENT)
        scale(scale, scale)
        textLineList.fold(14f) { sum, textLine ->
            drawTextLine(textLine, 0f, sum, paint)
            sum + height
        }
    }
}