package ksn

import androidx.compose.ui.geometry.Offset
import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Shape
import ksn.ui.SkiaDragStatus
import ksn.update.DragStatus
import ksn.model.shape.Rect as KsnRect
import org.jetbrains.skia.Rect as SkiaRect
import rtree.Rectangle as RTreeRectangle
import rtree.Point as RTreePoint

//FromSkiaConverters

fun SkiaDragStatus.toDragStatus() = DragStatus(
    start.toKsnPoint(),
    end.toKsnPoint(),
)

// ToSkiaConverters

fun Shape.toSkiaRect() = SkiaRect(
    this.left.toSkiaFloat(),
    this.top.toSkiaFloat() * 2,
    (this.right + 1).toSkiaFloat(),
    (this.bottom + 1).toSkiaFloat() * 2
)

//SkiaInternalConverters

fun SkiaDragStatus.toSkiaRect(): SkiaRect {
    val (left, right) = orderedPair(start.x, end.x)
    val (top, bottom) = orderedPair(start.y, end.y)
    return SkiaRect(left, top, right, bottom)
}

// ToRTreeConverters

fun Point.toRTreePoint(): RTreePoint = RTreePoint(this.x, this.y)

fun Shape.toRTreeRectangle() = RTreeRectangle(
    left,
    top,
    right,
    bottom
)

fun DragStatus.toRTreeRectangle(): RTreeRectangle {
    val (left, top, right ,bottom) = toDataRect()
    return RTreeRectangle(
        left,
        top,
        right,
        bottom
    )
}

//KsnConverters

fun DragStatus.toKsnRect(): KsnRect {
    val (left, top, right ,bottom) = toDataRect()
    return KsnRect(
        left,
        top,
        right,
        bottom
    )
}

fun DragStatus.toKsnLine(): Line {
    return Line(
        start,
        end
    )
}

private fun DragStatus.toDataRect(): DataRect {
    val (left, right) = orderedPair(start.x, end.x)
    val (top, bottom) = orderedPair(start.y, end.y)
    return DataRect(
        left,
        top,
        right,
        bottom
    )
}

private fun <T : Comparable<T>> orderedPair(a: T, b: T ) = if (a < b) {
    a to b
} else {
    b to a
}

private fun Float.toKsnUnit() = (this / GRID_WIDTH).toInt()

private fun Offset.toKsnPoint() = Point(
    x.toKsnUnit(),
    (y / 2).toKsnUnit()
)

private data class DataRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
