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
import ksn.update.ShapeWithID
import ksn.update.withId

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
        model.shapes.map {
            // TODO move to Shape#drag
            transformShapes(model, it, translateIdList)
        }.map { (_, shape) ->
            transformLines(model, shape, translateIdList)
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

/**
 * caution: update translateIdList
 */
private fun transformShapes(
    model: AppModel,
    shapeWithId: ShapeWithID,
    translateIdList: MutableSet<Long>
): ShapeWithID {
    val id = shapeWithId.id
    return if (model.selectShapeIdSet.contains(id)) {
        when (val dragType = model.dragType) {
            is DragType.DragMoving -> {
                translateIdList.add(id)
                shapeWithId.shape.translate(dragType.point).withId(id)
            }
            is DragType.DragResize -> shapeWithId.shape.resize(
                dragType.point,
                dragType.handlePosition
            ).withId(id)

            else -> shapeWithId
        }
    } else {
        shapeWithId
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
