package ksn.ascii

sealed class AsciiChar {
    abstract val value: String

    data class Char(override val value: String) : AsciiChar()
    object FullWidthSpace: AsciiChar() {
        override val value = ""
    }
    object Space: AsciiChar() {
        override val value = " "
    }
}
