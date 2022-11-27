package ksn.ascii

import ksn.model.Point
import ksn.model.minus
import ksn.model.shape.Line
import ksn.model.shape.Rect
import kotlin.math.ceil

fun Rect.toAsciiMatrix(): Matrix<AsciiChar> {
    val matrix = Matrix.init<AsciiChar>(width, height, AsciiChar.Char(Ascii.SPACE))
    this.toPointList()
        .toBoundingPointsList()
        .filterIsInstance<Edge>()
        .map(::toBounding)
        .run {
            toMutableList() + first()
        }
        .complementStroke()
        .forEach { (point, boundingType) ->
            matrix.set(point.x, point.y, AsciiChar.Char(boundingType.char))
        }
    return matrix
}

fun Line.toAsciiMatrix(): Matrix<AsciiChar> {
    val matrix = Matrix.init<AsciiChar>(width, height, AsciiChar.Char(Ascii.SPACE))
    val landscape = width > height
    val boundingList = this.toPointList(landscape)
        .toBoundingPointsList()
        .map(::toBounding)
    val boxDrawing = boundingList.complementStroke() + boundingList.last()

    boxDrawing.forEach { (point, boundingType) ->
        matrix.set(point.x, point.y, AsciiChar.Char(boundingType.char))
    }

    val point = boxDrawing.last().point
    val triangle = if (width > height) {
        if (point.x == 0) {
            BoundingType.LEFT_TRIANGLE
        } else {
            BoundingType.RIGHT_TRIANGLE
        }
    } else {
        if (point.y == 0) {
            BoundingType.UP_TRIANGLE
        } else {
            BoundingType.DOWN_TRIANGLE
        }
    }
    matrix.set(point.x, point.y, AsciiChar.Char(triangle.char))
    return matrix
}

private fun Rect.toPointList(): List<Point> {
    val points = arrayOf(
        Point(0, 0),
        Point(width - 1, 0),
        Point(width - 1, height - 1),
        Point(0, height - 1)
    )
    return listOf(
        points.last(),
        *points,
        points.first()
    )
}

private fun Line.toPointList(landscape: Boolean): List<Point> {
    val split = if (landscape) {
        ceil(width / 2f).toInt() - 1
    } else {
        ceil(height / 2f).toInt() - 1
    }
    val (p2, p3) = if (landscape) {
        Point(split, 0) to Point(split, height - 1)
    } else {
        Point(0, split) to Point(width - 1, split)
    }
    val offsetStart = start - Point(left, top)
    val offsetEnd = end - Point(left, top)

    return if (landscape) {
        if ((start.x > end.x && start.y > end.y) || (start.x < end.x && start.y > end.y)) {
            listOf(offsetStart, p3, p2, offsetEnd)
        } else {
            listOf(offsetStart, p2, p3, offsetEnd)
        }
    } else {
        if ((start.x > end.x && start.y > end.y) || (start.x > end.x && start.y < end.y)) {
            listOf(offsetStart, p3, p2, offsetEnd)
        } else {
            listOf(offsetStart, p2, p3, offsetEnd)
        }
    }
}

private fun List<Point>.toBoundingPointsList(): List<BoundingPoints> =
    List(this.size) { index ->
        val a = index - 1
        val b = index + 2
        when {
            a < 0 -> this.slice(0..1)
            b > this.size -> this.takeLast(2).reversed()
            else -> this.slice(a..a + 2)
        }.toBoundingPoints()
    }

private fun toBounding(boundingPoints: BoundingPoints): Bounding = when (boundingPoints) {
    is Edge -> Bounding(
        boundingPoints.value.second,
        boundingPoints.toBoundingType()
    )
    is Side -> Bounding(
        boundingPoints.value.first,
        boundingPoints.toBoundingType()
    )
}

private fun List<Bounding>.complementStroke(): List<Bounding> =
    this.toMutableList()
        .zipWithNext()
        .flatMap { (a, b) ->
            mutableListOf(a) + when {
                a.point.x == b.point.x -> (a.point.y between b.point.y)
                    .map { y ->
                        Bounding(Point(a.point.x, y), BoundingType.VERTICAL)
                    }
                a.point.y == b.point.y -> (a.point.x between b.point.x)
                    .map { x ->
                        Bounding(Point(x, a.point.y), BoundingType.HORIZONTAL)
                    }
                else -> throw IllegalArgumentException("Illegal corner pair")
            }
        }

private fun List<Point>.toBoundingPoints(): BoundingPoints = when (this.size) {
    2 -> Side(
        Pair(this[0], this[1])
    )
    3 -> Edge(
        Triple(this[0], this[1], this[2])
    )
    else -> throw IllegalArgumentException("List size is 2 or 3")
}

private fun Edge.toBoundingType(
    reversed: Boolean = false
): BoundingType {
    val (a, b, c) = this.value
    return when {
        a.x == b.x && b.x == c.x -> BoundingType.VERTICAL
        a.y == b.y && b.y == c.y -> BoundingType.HORIZONTAL
        a.y == b.y && b.x == c.x && a.x < b.x && b.y < c.y -> BoundingType.DOWN_AND_LEFT
        a.y == b.y && b.x == c.x && a.x > b.x && b.y < c.y -> BoundingType.DOWN_AND_RIGHT
        a.x == b.x && b.y == c.y && a.y < b.y && b.x > c.x -> BoundingType.UP_AND_LEFT
        a.x == b.x && b.y == c.y && a.y < b.y && b.x < c.x -> BoundingType.UP_AND_RIGHT
        !reversed -> Edge(Triple(c, b, a)).toBoundingType(true)
        else -> throw IllegalArgumentException("Illegal point combination in Corner")
    }
}

private fun Side.toBoundingType(): BoundingType {
    val (a, b) = this.value
    return if (a.x == b.x) {
        BoundingType.VERTICAL
    } else {
        BoundingType.HORIZONTAL
    }
}

private infix fun Int.between(other: Int): IntProgression = if (this < other) {
    this + 1 until other
} else {
    ( other + 1 until this).reversed()
}
