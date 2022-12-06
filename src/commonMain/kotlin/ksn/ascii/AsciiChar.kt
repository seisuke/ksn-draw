package ksn.ascii

import ksn.ascii.BoundingType.HORIZONTAL
import ksn.ascii.BoundingType.VERTICAL

sealed class AsciiChar {
    abstract val value: String

    data class Char(override val value: String) : AsciiChar()

    data class Bounding(val boundingType: BoundingType) : AsciiChar() {
        override val value: String
            get() = boundingType.char

        operator fun plus(other: Bounding): AsciiChar = when {
            this.boundingType == VERTICAL && other.boundingType == HORIZONTAL -> {
                Char("┼")
            }
            this.boundingType == HORIZONTAL && other.boundingType == VERTICAL -> {
                Char("┼")
            }
            else -> other
        }
    }

    object FullWidthSpace: AsciiChar() {
        override val value = ""
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
    else -> other
}
