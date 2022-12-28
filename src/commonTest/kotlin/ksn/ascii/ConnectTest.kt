package ksn.ascii

import ksn.ascii.TestHelper.assertAscii
import ksn.ascii.TestHelper.convertTextList
import ksn.model.HandlePosition
import ksn.model.Point
import ksn.model.shape.Line
import ksn.model.shape.Rect
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class ConnectTest {

    @Test
    fun translateConnectedShape() {

        val line1 = Line(
            Point(1, 1),
            Point(6, 3),
            Line.Connect.End(2, HandlePosition.LEFT_MIDDLE)
        )

        val rect1 = Rect(
            7,2,9,4,
            listOf(1)
        )

        val text1 = convertTextList(
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
        assertAscii(
            expect1,
            text1,
        )

        val rect2 = rect1.translate(Point(2, 1))
        val line2 = line1.connectTranslate(Point(2, 1), 2)
        val text2 = convertTextList(
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
        assertAscii(
            expect2,
            text2,
        )
    }

    @Test
    fun bothConnectedShape() {
        val line1 = Line(
            Point(4, 1),
            Point(10, 7),
            Line.Connect.Both(
                Line.Connect.Start(1, HandlePosition.CENTER_TOP),
                Line.Connect.End(2, HandlePosition.CENTER_BOTTOM),
            )
        )

        val rect1 = Rect(
            2,2,6, 6,
            listOf(3)
        )
        val rect2 = Rect(
            8,2,12, 6,
            listOf(3)
        )

        val text1 = convertTextList(
            listOf(line1, rect1, rect2),
            12,
            10
        )

        val expect1 =
            """|    ┌──┐     
               |    │  │     
               |  ┌───┐│┌───┐
               |  │   │││   │
               |  │   │││   │
               |  │   │││   │
               |  └───┘│└───┘
               |       │  ▲  
               |       └──┘  
               |             
               |             
            """.trimMargin()

        assertAscii(
            expect1,
            text1,
        )
    }

    @Test
    fun bothConnectedShape2() {
        val line1 = Line(
            Point(5, 3),
            Point(9, 7),
            Line.Connect.Both(
                Line.Connect.Start(1, HandlePosition.RIGHT_MIDDLE),
                Line.Connect.End(2, HandlePosition.CENTER_TOP),
            )
        )

        val rect1 = Rect(
            2,2,4, 4,
            listOf(3)
        )
        val rect2 = Rect(
            8,8,10, 10,
            listOf(3)
        )

        val text1 = convertTextList(
            listOf(line1, rect1, rect2),
            10,
            10
        )

        // TODO line shape is still strange
        val expect1 =
            """|           
               |           
               |  ┌─┐      
               |  │ │─┐    
               |  └─┘ └──┐ 
               |         │ 
               |         │ 
               |         ▼ 
               |        ┌─┐
               |        │ │
               |        └─┘
            """.trimMargin()

        assertAscii(
            expect1,
            text1,
        )
    }

}

