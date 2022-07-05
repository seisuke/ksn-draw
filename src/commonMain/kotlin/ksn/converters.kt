package ksn

import androidx.compose.ui.geometry.Offset
import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.DataRect
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import ksn.ui.DragStatus
import ksn.update.IntDragStatus
import org.jetbrains.skia.Rect as SkiaRect
import rtree.Rectangle as RTreeRectangle

//FromSkiaConverters

fun DragStatus.toIntDragStatus() = IntDragStatus(
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

fun DragStatus.toSkiaRect(): SkiaRect {
    val (left, right) = orderedPair(start.x, end.x)
    val (top, bottom) = orderedPair(start.y, end.y)
    return SkiaRect(left, top, right, bottom)
}

//KsnConverters

fun IntDragStatus.toDataRect(): DataRect {
    val (left, right) = orderedPair(start.x, end.x)
    val (top, bottom) = orderedPair(start.y, end.y)
    return DataRect(
        left,
        top,
        right,
        bottom
    )
}

fun IntDragStatus.toRTreeRectangle(): RTreeRectangle {
    val (left, top, right ,bottom) = toDataRect()
    return RTreeRectangle(
        left,
        top,
        right,
        bottom
    )
}

fun IntDragStatus.toKsnRect(id: Long): Rect {
    val (left, top, right ,bottom) = toDataRect()
    return Rect(
        id,
        left,
        top,
        right,
        bottom
    )
}

fun IntDragStatus.toKsnLine(id: Long): Line {
    return Line(
        id,
        start,
        end
    )
}

fun Shape.toRTreeRectangle() = RTreeRectangle(
    left,
    top,
    right,
    bottom
)

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
