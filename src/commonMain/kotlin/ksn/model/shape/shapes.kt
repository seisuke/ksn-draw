package ksn.model.shape

import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.Point
import kotlin.math.max
import kotlin.math.min

sealed interface Shape {
    val id: Long
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
    val width: Int
        get() = right - left + 1
    val height: Int
        get() = bottom - top + 1
    val isEmpty: Boolean
        get() = width <= 1 || height <= 1

    fun Int.toSkiaFloat(): Float = (this * GRID_WIDTH).toFloat()
}

data class Rect(
    override val id: Long,
    override val left: Int,
    override val top: Int,
    override val right: Int,
    override val bottom: Int
): Shape {
    companion object {
        fun initRect(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            idGenerator: () -> Long
        ): Rect = Rect(
            id = idGenerator(),
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }
}

data class Line(
    override val id: Long,
    val start: Point,
    val end: Point
): Shape {
    override val left: Int = min(start.x, end.x)
    override val top: Int = min(start.y, end.y)
    override val right: Int = max(start.x, end.x)
    override val bottom: Int = max(start.y, end.y)
}

