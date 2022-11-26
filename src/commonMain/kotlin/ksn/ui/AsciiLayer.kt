package ksn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ksn.ModelElement
import ksn.ascii.Ascii
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
    val shapes by element.mapAsState { model ->
        model.shapes.map { (id, shape) ->
            if (model.selectShapeIdList.contains(id)) {
                shape.translate(model.drag)
            } else {
                shape
            }
        }
    }
    val typeface by element.mapAsState(AppModel::typeface)

    ascii.matrix.clear()
    ascii.mergeToMatrix(shapes)

    Layer(
        width * scale,
        width * 2 * scale,
    ) { paint ->
        paint.color = 0xFF000000.toInt()
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
