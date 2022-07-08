package ksn.model

data class Point(
    val x: Int,
    val y: Int
) {
    companion object {
        val Zero = Point(0, 0)
    }
}

operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)
