package ksn

import androidx.compose.ui.geometry.Offset
import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.update.DragStatus

fun DragStatus.toKsnRect(id: Long): Rect {
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
    return Rect(
        id,
        left.toKsnUnit(),
        (top / 2).toKsnUnit(),
        right.toKsnUnit(),
        (bottom / 2).toKsnUnit()
    )
}

fun DragStatus.toKsnLine(id: Long): Line {
    return Line(
        id,
        start.toKsnPoint(),
        end.toKsnPoint()
    )
}

private fun Float.toKsnUnit() = (this / GRID_WIDTH).toInt()

private fun Offset.toKsnPoint() = Point(
    x.toKsnUnit(),
    (y / 2).toKsnUnit()
)
