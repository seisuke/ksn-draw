package ksn.model.shape

import ksn.Constants.Companion.GRID_WIDTH
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
}

