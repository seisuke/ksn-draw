package ksn.ascii

import ksn.model.Point

data class Bounding(
    val point: Point,
    val boundingType: BoundingType
)

enum class BoundingType(val char: String) {
    VERTICAL("│"),
    HORIZONTAL("─"),
    DOWN_AND_RIGHT("┌"),
    DOWN_AND_LEFT("┐"),
    UP_AND_LEFT("┘"),
    UP_AND_RIGHT("└"),
    VERTICAL_AND_HORIZONTAL("┼"),
    DOWN_AND_HORIZONTAL("┬"),
    UP_AND_HORIZONTAL("┴"),
    VERTICAL_AND_LEFT("┤"),
    VERTICAL_AND_RIGHT("├"),

}

enum class Triangle(val char: String) {
    RIGHT_TRIANGLE("▶"),
    LEFT_TRIANGLE("◀"),
    UP_TRIANGLE("▲"),
    DOWN_TRIANGLE("▼"),
}

