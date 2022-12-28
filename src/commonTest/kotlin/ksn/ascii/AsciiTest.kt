package ksn.ascii

import ksn.ascii.TestHelper.assertAscii
import ksn.ascii.TestHelper.convertTextList
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import kotlin.test.Test

class AsciiTest {
    @Test
    fun lineMatrix1() {
        val line = Line(
            Point(2, 6),
            Point(7, 8),
        )
        val actual = convertTextList(line)

        assertAscii(
            listOf(
                "──┐   ",
                "  │   ",
                "  └──▶"
            ),
            actual,
        )
    }

    @Test
    fun lineMatrixPortrait1() {
        val line = Line(
            Point(2, 8),
            Point(7, 3),
        )
        val actual = convertTextList(line)
        assertAscii(
            listOf(
                "     ▲",
                "     │",
                "┌────┘",
                "│     ",
                "│     ",
                "│     ",
            ),
            actual,
        )
    }

    @Test
    fun lineMatrixPortrait2() {
        val line = Line(
            Point(7, 8),
            Point(2, 3),
        )
        val actual = convertTextList(line)
        assertAscii(
            listOf(
                "▲     ",
                "│     ",
                "└────┐",
                "     │",
                "     │",
                "     │",
            ),
            actual,
        )
    }

    @Test
    fun lineMatrixPortrait3() {
        val line = Line(
            Point(7, 3),
            Point(2, 8),
        )
        val actual = convertTextList(line)
        assertAscii(
            listOf(
                "     │",
                "     │",
                "┌────┘",
                "│     ",
                "│     ",
                "▼     ",
            ),
            actual,
        )
    }

    @Test
    fun lineMatrixStraight1() {
        val line = Line(
            Point(4, 7),
            Point(7, 7),
        )
        val actual = convertTextList(line)

        assertAscii(
            listOf(
                "───▶",
            ),
            actual,
        )
    }

    @Test
    fun lineMatrixStraight2() {
        val line = Line(
            Point(4, 6),
            Point(4, 4),
        )
        val actual = convertTextList(line)

        assertAscii(
            listOf(
                "▲",
                "│",
                "│",
            ),
            actual,
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
        val actual = convertTextList(
            listOf(rect),
            5,
            5,
        )
        val expect =
            """|  │ │ 
               |  └─┘ 
               |      
               |      
               |      
               |      
            """.trimMargin()

        assertAscii(
            expect,
            actual,
        )
    }
}
