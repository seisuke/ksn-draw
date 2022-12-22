package ksn.model.shape

import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.DragType
import ksn.model.HandlePosition
import ksn.model.HandlePosition.CENTER_BOTTOM
import ksn.model.HandlePosition.CENTER_TOP
import ksn.model.HandlePosition.LEFT_BOTTOM
import ksn.model.HandlePosition.LEFT_MIDDLE
import ksn.model.HandlePosition.LEFT_TOP
import ksn.model.HandlePosition.RIGHT_BOTTOM
import ksn.model.HandlePosition.RIGHT_MIDDLE
import ksn.model.HandlePosition.RIGHT_TOP
import ksn.model.Point
import ksn.model.plus
import kotlin.math.max
import kotlin.math.min

sealed interface Shape {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
    val width: Int
        get() = right - left + 1
    val height: Int
        get() = bottom - top + 1
    val isEmpty: Boolean

    fun Int.toSkiaFloat(): Float = (this * GRID_WIDTH).toFloat()

    fun translate(point: Point): Shape

    fun resize(point: Point, handlePosition: HandlePosition): Shape

    //FIXME subclass type is removed
    fun drag(dragType: DragType): Shape = when (dragType) {
        is DragType.DragMoving -> translate(dragType.point)
        is DragType.DragResize -> resize(
            dragType.point,
            dragType.handlePosition
        )
        else -> this
    }
}

data class Rect(
    override val left: Int,
    override val top: Int,
    override val right: Int,
    override val bottom: Int
): Shape {
    override val isEmpty: Boolean
        get() = width <= 1 || height <= 1

    override fun translate(point: Point): Shape = Rect(
        left + point.x,
        top + point.y,
        right + point.x,
        bottom + point.y
    )

    // TODO validate point
    override fun resize(point: Point, handlePosition: HandlePosition): Shape = when (handlePosition) {
        LEFT_TOP -> Rect(
            left = left + point.x,
            top = top + point.y,
            right = right,
            bottom = bottom,
        )
        LEFT_MIDDLE -> Rect(
            left = left + point.x,
            top = top,
            right = right,
            bottom = bottom,
        )
        LEFT_BOTTOM -> Rect(
            left = left + point.x,
            top = top,
            right = right,
            bottom = bottom + point.y,
        )
        CENTER_BOTTOM -> Rect(
            left = left,
            top = top,
            right = right,
            bottom = bottom + point.y,
        )
        RIGHT_BOTTOM -> Rect(
            left = left,
            top = top,
            right = right + point.x,
            bottom = bottom + point.y,
        )
        RIGHT_MIDDLE -> Rect(
            left = left,
            top = top,
            right = right + point.x,
            bottom = bottom,
        )
        RIGHT_TOP -> Rect(
            left = left,
            top = top + point.y,
            right = right + point.x,
            bottom = bottom,
        )
        CENTER_TOP -> Rect(
            left = left,
            top = top + point.y,
            right = right,
            bottom = bottom,
        )
    }
}

data class Line(
    val start: Point,
    val end: Point
): Shape {
    override val left: Int = min(start.x, end.x)
    override val top: Int = min(start.y, end.y)
    override val right: Int = max(start.x, end.x)
    override val bottom: Int = max(start.y, end.y)

    override val isEmpty: Boolean
        get() = width <= 1 && height <= 1

    override fun translate(point: Point) = Line(
        start + point,
        end + point,
    )

    override fun resize(point: Point, handlePosition: HandlePosition): Shape = this
}

data class TextBox(
    val rect: Rect,
    val text: String,
): Shape by rect {

    override fun translate(point: Point): Shape = TextBox(
        rect = rect.translate(point) as Rect,
        text = text,
    )

    override fun resize(point: Point, handlePosition: HandlePosition): Shape = TextBox(
        rect = rect.resize(point, handlePosition) as Rect,
        text = text,
    )
}

