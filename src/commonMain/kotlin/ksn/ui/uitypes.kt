package ksn.ui

import ksn.model.shape.Line
import ksn.model.shape.Rect
import org.jetbrains.skia.Rect as SkiaRect

sealed interface UiType {
    fun draw(drawCanvas: () -> Unit) = drawCanvas.invoke()
}

data class SelectedShape(
    val skiaRectList: List<SkiaRect>
) : UiType

data class Selecting(
    val skiaRect: SkiaRect
) : UiType

data class AsciiRect(
    val rect: Rect,
) : UiType

data class AsciiLine(
    val line: Line,
) : UiType
