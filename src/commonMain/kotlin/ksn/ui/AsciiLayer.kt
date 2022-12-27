package ksn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import ksn.ModelElement
import ksn.ascii.Ascii
import ksn.ascii.AsciiRenderer
import ksn.model.DragType
import ksn.model.shape.Line
import ksn.model.shape.Shape
import ksn.update.AppModel

@Composable
fun AsciiLayer(
    width: Dp,
    scale: Float,
    ascii: Ascii,
) {
    val element = ModelElement.current
    val shapes by element.mapAsState { model ->
        val translateIdList = mutableSetOf<Long>()

        // temporary transform for UI
        val cloneShapeMap = model.shapes.clone()
        model.selectShapeIdSet.forEach { id ->
            cloneShapeMap.update(id) { shape ->
                when (val dragType = model.dragType) {
                    is DragType.DragMoving -> {
                        translateIdList.add(id)
                        shape.translate(dragType.point)
                    }
                    is DragType.DragResize -> shape.resize(
                        dragType.point,
                        dragType.handlePosition
                    )
                    else -> null
                }
            }
            cloneShapeMap.updateAllInstance<Line> { (_, line) ->
                when (val dragType = model.dragType) {
                    is DragType.DragMoving -> {
                        line.getConnectIdList().intersect(translateIdList).fold(line) { _: Line, shapeId: Long ->
                            line.connectTranslate(dragType.point, shapeId)
                        }
                    }
                    else -> null
                }
            }
        }
        cloneShapeMap.values.toList()
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

private fun transformLines(
    model: AppModel,
    shape: Shape,
    translateIdList: Set<Long>
) = if (shape is Line) {
    when (val dragType = model.dragType) {
        is DragType.DragMoving -> {
            shape.getConnectIdList().intersect(translateIdList).fold(shape) { _: Line, id: Long ->
                shape.connectTranslate(dragType.point, id)
            }
        }
        else -> shape
    }
} else {
    shape
}
