package ksn.ascii

import ksn.model.HandlePosition
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class ConnectTest {

    @Test
    fun test1() {

        val line1 = Line(
            Point(1, 1),
            Point(6, 3),
            Line.Connect.End(2, HandlePosition.LEFT_MIDDLE)
        )

        val rect1 = Rect(
            7,2,9,4,
            listOf(1)
        )

        val text1 = TestHelper.convertTextList(
            listOf(line1, rect1),
            12,
            6
        )

        val expect1 =
            """|             
               | ──┐         
               |   │   ┌─┐   
               |   └──▶│ │   
               |       └─┘   
               |             
               |             
            """.trimMargin()
        assertEquals(
            expect1,
            text1,
        )

        val rect2 = rect1.translate(Point(2, 1))
        val line2 = line1.connectTranslate(Point(2, 1), 2)
        val text2 = TestHelper.convertTextList(
            listOf(line2, rect2),
            12,
            6
        )

        val expect2 =
            """|             
               | ───┐        
               |    │        
               |    │    ┌─┐ 
               |    └───▶│ │ 
               |         └─┘ 
               |             
            """.trimMargin()
        assertEquals(
            expect2,
            text2,
        )
    }
}

