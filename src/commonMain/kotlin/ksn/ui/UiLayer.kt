package ksn.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import elm.Element
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import ksn.ModelElement
import ksn.model.Tool
import ksn.toDragStatus
import ksn.toSkiaRect
import ksn.update.AppModel
import ksn.update.DragStatus
import ksn.update.Msg
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PathEffect

@Composable
fun UiLayer(width: Dp, scale: Float) {
    val element = ModelElement.current

    val skiaDragStatusFlow = MutableStateFlow(SkiaDragStatus.Zero)
    val dragStatus by skiaDragStatusFlow.collectAsState()
    val tool by element.flowMapAsState { modelStateFlow ->
        modelStateFlow.value.tool to modelStateFlow.combineTransform(skiaDragStatusFlow) { model, dragStatus ->
            if (dragStatus != SkiaDragStatus.Zero) {
                emit(model.tool)
            }
        }
    }
    val shapes by element.mapAsState(AppModel::selectedShapes)
    val drag by element.mapAsState(AppModel::drag)

    val primaryColor = MaterialTheme.colors.primary.toArgb()

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
        nativeCanvas.apply {
            if (tool is Tool.Select) {
                if (shapes.isEmpty()) {
                    if (dragStatus == SkiaDragStatus.Zero) {
                        return@Layer
                    }
                    paint.color = primaryColor
                    paint.mode = PaintMode.STROKE
                    paint.strokeWidth = 2f
                    paint.pathEffect = PathEffect.makeDash(floatArrayOf(5f, 5f), 0f)
                    drawRect(dragStatus.toSkiaRect(), paint)
                } else {
                    paint.color = primaryColor
                    paint.mode = PaintMode.STROKE
                    shapes.forEach { (_, shape) ->
                        drawRect(shape.translate(drag).toSkiaRect(), paint)
                    }
                }
            }
        }
    }
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
