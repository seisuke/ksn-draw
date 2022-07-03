package ksn.ascii

import ksn.model.Point
import ksn.model.shape.Line
import kotlin.test.Test
import kotlin.test.assertEquals

class AsciiTest {
    @Test
    fun lineMatrix1() {
        val line = Line(
            1L,
            Point(2, 3),
            Point(7, 8),
        )
        val text = convertTextList(line)

        assertEquals(
            text,
            listOf(
                "──┐   ",
                "  │   ",
                "  │   ",
                "  │   ",
                "  │   ",
                "  └──▶"
            )
        )
    }

    @Test
    fun lineMatrix2() {
        val line = Line(
            1L,
            Point(2, 8),
            Point(7, 3),
        )
        val text = convertTextList(line)

        assertEquals(
            text,
            listOf(
                "  ┌──▶",
                "  │   ",
                "  │   ",
                "  │   ",
                "  │   ",
                "──┘   "
            )
        )
    }

    @Test
    fun lineMatrix3() {
        val line = Line(
            1L,
            Point(7, 8),
            Point(2, 3),
        )
        val text = convertTextList(line)

        assertEquals(
            text,
            listOf(
                "◀─┐   ",
                "  │   ",
                "  │   ",
                "  │   ",
                "  │   ",
                "  └───"
            )
        )
    }

    private fun convertTextList(line: Line): List<String> {
        val text = Ascii(
            Matrix.init(0, 0, AsciiChar.Char(" "))
        ).run {
            line.toAsciiMatrix().joinToString { it.value }
        }
        return text
    }
}
