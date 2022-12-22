package ksn.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import ksn.model.DragType
import ksn.model.HandlePosition
import ksn.model.Point
import ksn.model.SelectState
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
    val skiaMouseCursorFlow = MutableStateFlow(Offset.Zero)
    val primaryColor = MaterialTheme.colors.primary.toArgb()
    val typeface by element.mapAsState(AppModel::typeface)
    val uiTypeList by element.flowMapAsState(
        listOf(Selecting(SkiaRect(0f,0f,0f, 0f)) as UiType)
    ) { modelStateFlow ->
        combineTransform(
            modelStateFlow,
            skiaDragStatusFlow,
            skiaMouseCursorFlow
        ) { model: AppModel, dragStatus: SkiaDragStatus, mouseCursor: Offset ->
            val uiTypeList = createUiType(
                model.tool,
                model.selectedShapes(),
                model.dragType,
                dragStatus,
                mouseCursor,
            )
            emit(uiTypeList)
        }
    }

    Layer(
        width * scale,
        width * 2 * scale,
        Modifier
            .handleDrag(
                skiaDragStatusFlow,
                element,
                scale
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val events = awaitPointerEvent()
                        val event = events.changes.first()
                        skiaMouseCursorFlow.value = event.position
                    }
                }
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
    dragType: DragType,
    dragStatus: SkiaDragStatus,
    mouseCursor: Offset, //TODO improve performance
): List<UiType> = when (tool) {
    is Tool.Select -> {
        val selectedShape = selectedShape(shapes, dragType)
        val selecting = if (dragStatus != SkiaDragStatus.Zero && tool.state == SelectState.None) {
            Selecting(dragStatus.toSkiaRect())
        } else  null
        val resizeShape = if (tool.state is SelectState.Resize) {
            val skiaRect = selectedShape.skiaRectList.first()
            val handleList = createHandleList(skiaRect, tool.state.handlePosition)
            ResizeShape(skiaRect, handleList)
        } else if (selectedShape.skiaRectList.size == 1) {
            val skiaRect = selectedShape.skiaRectList.first()
            val handleList = createHandleList(skiaRect, mouseCursor)
            ResizeShape(skiaRect, handleList)
        } else {
            null
        }
        listOfNotNull(
            selectedShape,
            selecting,
            resizeShape,
        )
    }
    is Tool.Rect,
    is Tool.Text -> {
        if (dragStatus == SkiaDragStatus.Zero) {
            val selectedShape = selectedShape(shapes, dragType)
            listOf(selectedShape)
        } else {
            val rect = dragStatus.toDragStatus().toKsnRect()
            listOf(
                AsciiRect(rect)
            )
        }
    }
    is Tool.Line -> {
        if (dragStatus == SkiaDragStatus.Zero) {
            val selectedShape = selectedShape(shapes, dragType)
            listOf(selectedShape)
        } else {
            val line = dragStatus.toDragStatus().toKsnLine()
            listOf(
                AsciiLine(line)
            )
        }
    }

    else -> emptyList()
}

private fun createHandleList(shape: SkiaRect, mouseCursor: Offset): List<Pair<SkiaRect, Boolean>> {
    return shape.createHandle(10f).map { (rect, _) ->
        rect to rect.inside(mouseCursor)
    }
}

private fun createHandleList(shape: SkiaRect, selectHandlePosition: HandlePosition): List<Pair<SkiaRect, Boolean>> {
    return shape.createHandle(10f).map { (rect, handlePosition) ->
        rect to (selectHandlePosition == handlePosition)
    }
}

private fun selectedShape(
    shapes: List<ShapeWithID>,
    dragType: DragType
): SelectedShape {
    return SelectedShape(
        shapes.map { it.shape.drag(dragType).toSkiaRect() }
    )
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
            paint.reset()
        }

        is Selecting -> uiType.draw {
            paint.color = primaryColor
            paint.mode = PaintMode.STROKE
            paint.strokeWidth = 2f
            paint.pathEffect = PathEffect.makeDash(floatArrayOf(5f, 5f), 0f)
            nativeCanvas.drawRect(uiType.skiaRect, paint)
            paint.reset()
        }

        is ResizeShape -> uiType.draw {
            paint.color = primaryColor
            uiType.handleList.forEach { (handle, selected) ->
                if (selected) {
                    paint.mode = PaintMode.STROKE_AND_FILL
                } else {
                    paint.mode = PaintMode.STROKE
                }
                nativeCanvas.drawRect(handle, paint)
            }
            paint.reset()
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
            paint.reset()
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
            paint.reset()
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
            AsciiChar.Space
        )
    )
    val noOffsetRect = shape.translate(
        Point(-shape.left, -shape.top)
    )
    ascii.mergeToMatrix(listOf(noOffsetRect))
    return ascii
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.handleDrag(
    skiaDragStatusFlow: MutableStateFlow<SkiaDragStatus>,
    element: Element<AppModel, Msg>,
    scale: Float
): Modifier {
    return this.onDrag(
        onDragStart = { offset ->
            val skiaDragStatus = SkiaDragStatus(offset / scale, offset / scale)
            element.accept(
                DragStatus.DragStart(skiaDragStatus)
            )
            skiaDragStatusFlow.value = skiaDragStatus
        },
        onDrag = { dragAmount ->
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
