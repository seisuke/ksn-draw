package ksn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import ksn.ModelElement
import ksn.ascii.Ascii
import ksn.ascii.AsciiRenderer
import ksn.update.AppModel

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
        AsciiRenderer.drawAscii(
            nativeCanvas,
            paint,
            loadedTypeface,
            ascii,
            scale
        )
    }
}
