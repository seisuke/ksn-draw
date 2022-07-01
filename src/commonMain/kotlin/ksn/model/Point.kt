package ksn.model

data class Point(
    val x: Int,
    val y: Int
)

operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)
