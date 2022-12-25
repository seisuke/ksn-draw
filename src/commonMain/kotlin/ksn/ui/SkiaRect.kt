package ksn.ui

import androidx.compose.ui.geometry.Offset
import ksn.model.HandlePosition
import org.jetbrains.skia.Rect as SkiaRect

fun SkiaRect.createResizeHandle(size: Float): List<Pair<SkiaRect, HandlePosition>> {
    val (vCenterTop, vCenterBottom) = halfPointPair(top, bottom, size)
    val (hCenterLeft, hCenterRight) = halfPointPair(left, right, size)
    return listOf(
        SkiaRect(left - 10, top - 10, left, top) to HandlePosition.LEFT_TOP,
        SkiaRect(left - 10, vCenterTop, left, vCenterBottom) to HandlePosition.LEFT_MIDDLE,
        SkiaRect(left - 10, bottom, left, bottom + 10) to HandlePosition.LEFT_BOTTOM,
        SkiaRect(hCenterLeft, bottom, hCenterRight, bottom + 10) to HandlePosition.CENTER_BOTTOM,
        SkiaRect(right, bottom, right + 10, bottom + 10) to HandlePosition.RIGHT_BOTTOM,
        SkiaRect(right, vCenterTop, right + 10, vCenterBottom) to HandlePosition.RIGHT_MIDDLE,
        SkiaRect(right, top - 10, right + 10, top) to HandlePosition.RIGHT_TOP,
        SkiaRect(hCenterLeft, top - 10, hCenterRight, top) to HandlePosition.CENTER_TOP,
    )
}

fun SkiaRect.inside(point: Offset): Boolean {
    return (this.left..this.right).contains(point.x) && (this.top..this.bottom).contains(point.y)
}

private fun halfPointPair(a: Float, b: Float, size: Float): Pair<Float, Float> {
    val center = (a + b) / 2
    return center - size / 2 to center + size / 2
}
