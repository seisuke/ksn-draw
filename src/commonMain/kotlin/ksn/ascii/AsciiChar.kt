package ksn.ascii

import androidx.compose.animation.slideOutHorizontally
import io.github.seisuke.kemoji.Emoji as Kemoji
import ksn.ascii.BoundingType.DOWN_AND_HORIZONTAL
import ksn.ascii.BoundingType.DOWN_AND_LEFT
import ksn.ascii.BoundingType.DOWN_AND_RIGHT
import ksn.ascii.BoundingType.HORIZONTAL
import ksn.ascii.BoundingType.UP_AND_HORIZONTAL
import ksn.ascii.BoundingType.UP_AND_LEFT
import ksn.ascii.BoundingType.UP_AND_RIGHT
import ksn.ascii.BoundingType.VERTICAL
import ksn.ascii.BoundingType.VERTICAL_AND_HORIZONTAL
import ksn.ascii.BoundingType.VERTICAL_AND_LEFT
import ksn.ascii.BoundingType.VERTICAL_AND_RIGHT

sealed class AsciiChar {
    abstract val value: String
    open val width: Int = 1

    data class Char(
        override val value: String,
        private val fullWidth: Boolean = false
    ) : AsciiChar() {
        override val width: Int
            get() = if (fullWidth) {
                2
            } else {
                1
            }
    }

    data class Bounding(val boundingType: BoundingType) : AsciiChar() {
        override val value: String = boundingType.char

        operator fun plus(other: Bounding): AsciiChar = when {
            eitherEqual(this, other, VERTICAL, HORIZONTAL) -> Bounding(VERTICAL_AND_HORIZONTAL)
            eitherEqual(this, other, UP_AND_LEFT, DOWN_AND_RIGHT) -> Bounding(VERTICAL_AND_HORIZONTAL)
            eitherEqual(this, other, UP_AND_RIGHT, DOWN_AND_LEFT) -> Bounding(VERTICAL_AND_HORIZONTAL)
            eitherEqual(this, other, HORIZONTAL, DOWN_AND_LEFT) -> Bounding(DOWN_AND_HORIZONTAL)
            eitherEqual(this, other, HORIZONTAL, DOWN_AND_RIGHT) -> Bounding(DOWN_AND_HORIZONTAL)
            eitherEqual(this, other, HORIZONTAL, UP_AND_LEFT) -> Bounding(UP_AND_HORIZONTAL)
            eitherEqual(this, other, HORIZONTAL, UP_AND_RIGHT) -> Bounding(UP_AND_HORIZONTAL)
            eitherEqual(this, other, VERTICAL, UP_AND_LEFT) -> Bounding(VERTICAL_AND_LEFT)
            eitherEqual(this, other, VERTICAL, DOWN_AND_LEFT) -> Bounding(VERTICAL_AND_LEFT)
            eitherEqual(this, other, VERTICAL, UP_AND_RIGHT) -> Bounding(VERTICAL_AND_RIGHT)
            eitherEqual(this, other, VERTICAL, DOWN_AND_RIGHT) -> Bounding(VERTICAL_AND_RIGHT)
            else -> this
        }

        private fun eitherEqual(a: Bounding, b: Bounding, aType: BoundingType, bType: BoundingType) =
            (a.boundingType == aType && b.boundingType == bType) || (a.boundingType == bType && b.boundingType == aType)

    }

    data class Emoji(
        val emoji: Kemoji,
        val x: Int,
        val y: Int
    ) : AsciiChar() {
        override val value: String = "  "
        override val width: Int = 2
    }

    object FullWidthSpace: AsciiChar() {
        override val value = ""
        override val width: Int = 0
    }
    object Space: AsciiChar() {
        override val value = " "
    }
    object Transparent: AsciiChar() {
        override val value = " "
    }
}

operator fun AsciiChar.plus(other: AsciiChar): AsciiChar = when {
    this is AsciiChar.Bounding && other is AsciiChar.Bounding -> this + other
    this is AsciiChar.Char && other is AsciiChar.Char -> other
    this is AsciiChar.Char -> this
    this is AsciiChar.FullWidthSpace -> this
    else -> other
}
