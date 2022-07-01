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
    RIGHT_TRIANGLE("▶"),
    LEFT_TRIANGLE("◀"),
    UP_TRIANGLE("▲"),
    DOWN_TRIANGLE("▼"),
}

