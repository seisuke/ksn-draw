package ksn.ascii

import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

class AsciiTest {
    @Test
    fun lineMatrix1() {
        val line = Line(
            Point(2, 6),
            Point(7, 8),
        )
        val text = convertTextList(line)

        assertEquals(
            listOf(
                "──┐   ",
                "  │   ",
                "  └──▶"
            ),
            text,
        )
    }

    @Test
    fun lineMatrixPortrait1() {
        val line = Line(
            Point(2, 8),
            Point(7, 3),
        )
        val text = convertTextList(line)
        assertEquals(
            listOf(
                "     ▲",
                "     │",
                "┌────┘",
                "│     ",
                "│     ",
                "│     ",
            ),
            text,
        )
    }

    @Test
    fun lineMatrixPortrait2() {
        val line = Line(
            Point(7, 8),
            Point(2, 3),
        )
        val text = convertTextList(line)
        assertEquals(
            listOf(
                "▲     ",
                "│     ",
                "└────┐",
                "     │",
                "     │",
                "     │",
            ),
            text,
        )
    }

    @Test
    fun lineMatrixPortrait3() {
        val line = Line(
            Point(7, 3),
            Point(2, 8),
        )
        val text = convertTextList(line)
        assertEquals(
            listOf(
                "     │",
                "     │",
                "┌────┘",
                "│     ",
                "│     ",
                "▼     ",
            ),
            text,
        )
    }

    @Test
    fun lineMatrixStraight1() {
        val line = Line(
            Point(4, 7),
            Point(7, 7),
        )
        val text = convertTextList(line)

        assertEquals(
            listOf(
                "───▶",
            ),
            text,
        )
    }

    @Test
    fun lineMatrixStraight2() {
        val line = Line(
            Point(4, 6),
            Point(4, 4),
        )
        val text = convertTextList(line)

        assertEquals(
            listOf(
                "▲",
                "│",
                "│",
            ),
            text,
        )
    }

    @Test
    fun overhangShape1() {
        val rect = Rect(
            2,
            -1,
            4,
            1
        )

        val ascii = Ascii(
            Matrix.init(5, 5, AsciiChar.Transparent)
        )
        ascii.mergeToMatrix(listOf(rect))
        val text = ascii.matrix.joinToString { it.value }

        assertEquals(
            listOf(
                "  │ │",
                "  └─┘",
                "     ",
                "     ",
                "     ",
            ),
            text,
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
