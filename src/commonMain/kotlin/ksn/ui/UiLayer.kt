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
import ksn.Constants.Companion.GRID_WIDTH
import ksn.Constants.Companion.LINE_ANCHOR_DISTANCE
import ksn.ModelElement
import ksn.ascii.Ascii
import ksn.ascii.AsciiChar
import ksn.ascii.AsciiRenderer
import ksn.ascii.Matrix
import ksn.model.DragType
import ksn.model.HandlePosition
import ksn.model.Point
import ksn.model.SelectState
import ksn.model.ShapeMap
import ksn.model.Tool
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import ksn.model.shape.TextBox
import ksn.model.shape.createAnchorHandle
import ksn.toDragStatus
import ksn.toKsnLine
import ksn.toKsnPoint
import ksn.toKsnRect
import ksn.toRTreePoint
import ksn.toSkiaOffset
import ksn.toSkiaRect
import ksn.update.AppModel
import ksn.update.DragStatus
import ksn.update.Msg
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Typeface
import rtree.RTree
import rtree.Rectangle
import ksn.model.Point as KsnPoint
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
                model.shapes,
                model.selectedShapes(),
                model.dragType,
                model.rtree,
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
    shapeMap: ShapeMap,
    selectedShapes: Map<Long, Shape>,
    dragType: DragType,
    rtree: RTree<Long, Rectangle>,
    skiaDragStatus: SkiaDragStatus,
    mouseCursor: Offset, //TODO improve performance
): List<UiType> = when (tool) {
    is Tool.Select -> {
        val selectedShape = selectedShape(selectedShapes, dragType)
        val selecting = if (skiaDragStatus != SkiaDragStatus.Zero && tool.state == SelectState.None) {
            Selecting(skiaDragStatus.toSkiaRect())
        } else  null
        val resizeShape = if (tool.state is SelectState.Resize) {
            val skiaRect = selectedShape.skiaRectList.first()
            val handleList = createResizeHandleList(skiaRect, tool.state.handlePosition)
            ResizeShape(skiaRect, handleList)
        } else if (selectedShape.skiaRectList.size == 1) {
            val skiaRect = selectedShape.skiaRectList.first()
            val handleList = createResizeHandleList(skiaRect, mouseCursor)
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
        if (skiaDragStatus == SkiaDragStatus.Zero) {
            val selectedShape = selectedShape(selectedShapes, dragType)
            listOf(selectedShape)
        } else {
            val rect = skiaDragStatus.toDragStatus().toKsnRect()
            listOf(
                AsciiRect(rect)
            )
        }
    }
    is Tool.Line -> {
        if (skiaDragStatus == SkiaDragStatus.Zero) {
            val point = mouseCursor.toKsnPoint()
            val anchorList = searchAnchorList(rtree, shapeMap, point)
            listOf(
                selectedShape(selectedShapes, dragType),
                LineAnchor(anchorList),
            )
        } else {
            val dragStatus = skiaDragStatus.toDragStatus()
            val line = dragStatus.toKsnLine()

            val point = dragStatus.end
            val anchorList = searchAnchorList(rtree, shapeMap, point)
            listOf(
                AsciiLine(line),
                LineAnchor(anchorList),
            )
        }
    }

    else -> emptyList()
}

private fun searchAnchorList(
    rtree: RTree<Long, Rectangle>,
    shapes: ShapeMap,
    point: Point,
): List<Anchor> {
    val nearShapeIdList = rtree.search(point.toRTreePoint(), LINE_ANCHOR_DISTANCE)
        .map { (id, _) -> id }
    val anchorList = shapes.filter { (id, shape) ->
        nearShapeIdList.contains(id) && (shape is Rect || shape is TextBox)
    }.flatMap { (id, shape) ->
        createAnchorHandleList(id, shape, point)
    }
    return anchorList
}

data class Anchor(
    val shapeId: Long,
    val handle: KsnPoint,
    val isSelect: Boolean,
)

private fun createAnchorHandleList(id: Long, shape: Shape, point: Point): List<Anchor> {
    val handleList = shape.createAnchorHandle()
    return handleList.map { handle ->
        Anchor(
            id,
            handle,
            handle == point
        )
    }
}

private fun createResizeHandleList(rect: SkiaRect, mouseCursor: Offset): List<Pair<SkiaRect, Boolean>> {
    return rect.createResizeHandle(10f).map { (rect, _) ->
        rect to rect.inside(mouseCursor)
    }
}

private fun createResizeHandleList(rect: SkiaRect, selectHandlePosition: HandlePosition): List<Pair<SkiaRect, Boolean>> {
    return rect.createResizeHandle(10f).map { (rect, handlePosition) ->
        rect to (selectHandlePosition == handlePosition)
    }
}

private fun selectedShape(
    shapes: Map<Long, Shape>,
    dragType: DragType
): SelectedShape {
    return SelectedShape(
        shapes.map { (_, shape) ->
            shape.drag(dragType).toSkiaRect()
        }
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
            val rect = uiType.rect
            drawAscii(rect, paint, primaryColor, typeface, scale)
        }

        is AsciiLine -> uiType.draw {
            val line = uiType.line
            drawAscii(line, paint, primaryColor, typeface, scale)
        }

        is LineAnchor -> uiType.draw {
            uiType.anchorList.forEach { anchor ->
                val offset = anchor.handle.toSkiaOffset()
                if (anchor.isSelect) {
                    paint.color = Color.RED // TODO Fix Color
                } else {
                    paint.color = Color.GREEN // TODO Fix Color
                }
                nativeCanvas.drawCircle(offset.x + GRID_WIDTH / 2, offset.y + GRID_WIDTH, GRID_WIDTH / 2f, paint)
            }
            paint.reset()
        }
    }
}

private fun Canvas.drawAscii(
    shape: Shape,
    paint: Paint,
    color: Int,
    typeface: Typeface,
    scale: Float
) {
    val ascii = shape.toAscii()
    val offset = shape.toOffset()
    paint.color = color
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

private fun Shape.toOffset(): Offset = Offset(
    left.toSkiaFloat(),
    top.toSkiaFloat() * 2
)

private fun Shape.toAscii(): Ascii {
    val ascii = Ascii(
        Matrix.init(
            width,
            height,
            AsciiChar.Space
        )
    )
    val noOffsetRect = translate(
        KsnPoint(-left, -top)
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
