package ksn.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import elm.Element
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import ksn.ModelElement
import ksn.ascii.Ascii
import ksn.ascii.AsciiChar
import ksn.ascii.AsciiRenderer
import ksn.ascii.Matrix
import ksn.model.Point
import ksn.model.Tool
import ksn.model.shape.Shape
import ksn.toDragStatus
import ksn.toKsnLine
import ksn.toKsnRect
import ksn.toSkiaRect
import ksn.update.AppModel
import ksn.update.DragStatus
import ksn.update.Msg
import ksn.update.ShapeWithID
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.Rect as SkiaRect

@Composable
fun UiLayer(width: Dp, scale: Float) {
    val element = ModelElement.current

    val skiaDragStatusFlow = MutableStateFlow(SkiaDragStatus.Zero)
    val primaryColor = MaterialTheme.colors.primary.toArgb()
    val typeface by element.mapAsState(AppModel::typeface)
    val uiTypeList by element.flowMapAsState { modelStateFlow ->
        val initValue: List<UiType> = listOf(
            Selecting(SkiaRect(0f,0f,0f, 0f))
        )
        initValue to modelStateFlow.combineTransform(skiaDragStatusFlow) { model, dragStatus ->
            val uiTypeList = createUiType(
                model.tool,
                model.selectedShapes(),
                model.drag,
                dragStatus,
            )
            emit(uiTypeList)
        }
    }

    Layer(
        width * scale,
        width * 2 * scale,
        Modifier.pointerInput(Unit) {
            createDetectDragGesture(
                skiaDragStatusFlow,
                element,
                scale
            )
        }
    ) { paint ->
        val loadedTypeface = typeface ?: return@Layer
        uiTypeList.forEach { uiType ->
            drawByUiType(uiType, paint, primaryColor, scale, loadedTypeface)
        }
    }
}

private fun createUiType(
    tool: Tool,
    shapes: List<ShapeWithID>,
    drag: Point,
    dragStatus: SkiaDragStatus,
): List<UiType> = when (tool) {
    is Tool.Select -> {
        val selectedShape = SelectedShape(
            shapes.map { it.shape.translate(drag).toSkiaRect() }
        )
        if (dragStatus == SkiaDragStatus.Zero || tool.moving) {
            listOf(selectedShape)
        } else {
            listOf(
                selectedShape,
                Selecting(dragStatus.toSkiaRect())
            )
        }
    }
    is Tool.Rect -> {
        if (dragStatus == SkiaDragStatus.Zero) {
            emptyList()
        } else {
            val rect = dragStatus.toDragStatus().toKsnRect()
            listOf(
                AsciiRect(rect)
            )
        }
    }
    is Tool.Line -> {
        if (dragStatus == SkiaDragStatus.Zero) {
            emptyList()
        } else {
            val line = dragStatus.toDragStatus().toKsnLine()
            listOf(
                AsciiLine(line)
            )
        }
    }

    else -> emptyList()
}

private fun Canvas.drawByUiType(
    uiType: UiType,
    paint: Paint,
    primaryColor: Int,
    scale: Float,
    typeface: Typeface,
) {
    when (uiType) {
        is SelectedShape -> uiType.draw {
            paint.color = primaryColor
            paint.mode = PaintMode.STROKE
            uiType.skiaRectList.forEach { rect ->
                nativeCanvas.drawRect(rect, paint)
            }
        }

        is Selecting -> uiType.draw {
            paint.color = primaryColor
            paint.mode = PaintMode.STROKE
            paint.strokeWidth = 2f
            paint.pathEffect = PathEffect.makeDash(floatArrayOf(5f, 5f), 0f)
            nativeCanvas.drawRect(uiType.skiaRect, paint)
        }

        is AsciiRect -> uiType.draw {
            paint.color = primaryColor
            val rect = uiType.rect
            val ascii = shapeToAscii(rect)
            val offset = offsetFromShape(rect)
            AsciiRenderer.drawAscii(
                nativeCanvas,
                paint,
                typeface,
                ascii,
                scale,
                offset.x,
                offset.y
            )
        }

        is AsciiLine -> uiType.draw {
            paint.color = primaryColor
            val line = uiType.line
            val ascii = shapeToAscii(line)
            val offset = offsetFromShape(line)
            AsciiRenderer.drawAscii(
                nativeCanvas,
                paint,
                typeface,
                ascii,
                scale,
                offset.x,
                offset.y
            )
        }
    }
}

private fun offsetFromShape(shape: Shape): Offset = with(shape) {
    Offset(
        left.toSkiaFloat(),
        top.toSkiaFloat() * 2
    )
}

private fun shapeToAscii(shape: Shape): Ascii {
    val ascii = Ascii(
        Matrix.init(
            shape.width,
            shape.height,
            AsciiChar.Char(Ascii.SPACE)
        )
    )
    val noOffsetRect = shape.translate(
        Point(-shape.left, -shape.top)
    )
    ascii.mergeToMatrix(listOf(noOffsetRect))
    return ascii
}

private suspend fun PointerInputScope.createDetectDragGesture(
    skiaDragStatusFlow: MutableStateFlow<SkiaDragStatus>,
    element: Element<AppModel, Msg>,
    scale: Float
) {
    detectDragGestures(
        onDragStart = { offset ->
            val skiaDragStatus = SkiaDragStatus(offset / scale, offset / scale)
            element.accept(
                DragStatus.DragStart(skiaDragStatus.toDragStatus())
            )
            skiaDragStatusFlow.value = skiaDragStatus
        },
        onDrag = { change, dragAmount ->
            change.consume()
            val oldDragStatus = skiaDragStatusFlow.value
            val newDragStatus = oldDragStatus.copy(
                end = oldDragStatus.end + dragAmount / scale
            )
            element.accept(
                DragStatus.Drag(newDragStatus.toDragStatus())
            )
            skiaDragStatusFlow.value = newDragStatus
        },
        onDragEnd = {
            element.accept(
                DragStatus.DragEnd(skiaDragStatusFlow.value.toDragStatus())
            )
            skiaDragStatusFlow.value = SkiaDragStatus.Zero
        },
    )
}
