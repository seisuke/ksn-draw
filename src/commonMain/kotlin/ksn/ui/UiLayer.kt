package ksn.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import elm.Element
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import ksn.ModelElement
import ksn.model.Tool
import ksn.model.shape.toSkiaRect
import ksn.update.AppModel
import ksn.update.DragStatus
import ksn.update.Msg
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Rect

@Composable
fun UiLayer(width: Dp, scale: Float) {
    val element = ModelElement.current

    val dragStatusFlow = MutableStateFlow(DragStatus.Zero)
    val dragStatus by dragStatusFlow.collectAsState()
    val tool by element.flowMapAsState { modelStateFlow ->
        modelStateFlow.value.tool to modelStateFlow.combineTransform(dragStatusFlow) { model, dragStatus ->
            if (dragStatus != DragStatus.Zero) {
                emit(model.tool)
            }
        }
    }
    val shapes by element.mapAsState(AppModel::selectedShapes)

    Layer(
        width * scale,
        width * 2 * scale,
        Modifier.pointerInput(Unit) {
            createDetectDragGesture(
                dragStatusFlow,
                element,
                scale
            )
        }
    ) { paint ->
        nativeCanvas.apply {
            if (tool is Tool.Select) {
                if (shapes.isEmpty()) {
                    if (dragStatus == DragStatus.Zero) {
                        return@Layer
                    }
                    paint.color = 0xFF0000FF.toInt()
                    paint.mode = PaintMode.STROKE
                    paint.strokeWidth = 2f
                    paint.pathEffect = PathEffect.makeDash(floatArrayOf(5f, 5f), 0f)
                    drawRect(dragStatus.toRect(), paint)
                } else {
                    paint.color = 0xFF0000FF.toInt()
                    paint.mode = PaintMode.STROKE
                    shapes.forEach { shape ->
                        drawRect(shape.toSkiaRect(), paint)
                    }
                }
            }
        }
    }
}

private fun DragStatus.toRect(): Rect {
    val (left, right) = if (start.x < end.x) {
        start.x to end.x
    } else {
        end.x to start.x
    }
    val (top, bottom) = if (start.y < end.y) {
        start.y to end.y
    } else {
        end.y to start.y
    }
    return Rect(left, top, right, bottom)
}

private suspend fun PointerInputScope.createDetectDragGesture(
    dragStatusFlow: MutableStateFlow<DragStatus>,
    element: Element<AppModel, Msg>,
    scale: Float
) {
    detectDragGestures(
        onDragStart = { offset ->
            dragStatusFlow.value = DragStatus(offset / scale, offset / scale)
        },
        onDrag = { change, dragAmount ->
            change.consume()
            val oldDragStatus = dragStatusFlow.value
            dragStatusFlow.value = oldDragStatus.copy(
                end = oldDragStatus.end + dragAmount / scale
            )
        },
        onDragEnd = {
            element.accept(
                DragStatus.DragEnd(dragStatusFlow.value)
            )
            dragStatusFlow.value = DragStatus.Zero
        },
    )
}
