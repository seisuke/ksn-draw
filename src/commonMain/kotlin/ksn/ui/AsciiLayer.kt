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
        val mutableShapeMap = model.shapes.toMutableShapeMap()
        model.selectShapeIdSet.forEach { id ->
            mutableShapeMap.update(id) { shape ->
                when (val dragType = model.dragType) {
                    is DragType.DragMoving -> {
                        translateIdList.add(id)
                        shape.translate(dragType.point)
                    }
                    is DragType.DragResize -> {
                        translateIdList.add(id)
                        shape.resize(
                            dragType.point,
                            dragType.handlePosition
                        )
                    }
                    else -> null
                }
            }
        }
        mutableShapeMap.updateAllInstance<Line> { (id, line) ->
            if (translateIdList.contains(id)) {
                return@updateAllInstance null
            }

            line.getConnectIdList().intersect(translateIdList).fold(line) { _: Line, shapeId: Long ->
                when (val dragType = model.dragType) {
                    is DragType.DragMoving -> {
                        line.connectTranslate(dragType.point, shapeId)
                    }
                    is DragType.DragResize -> {
                        val shape = mutableShapeMap[shapeId] ?: return@fold line
                        line.connectResize(shapeId, shape)
                    }
                    else -> line
                }
            }
        }
        mutableShapeMap.values.toList()
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
