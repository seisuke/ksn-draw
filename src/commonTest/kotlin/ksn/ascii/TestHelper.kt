package ksn.ascii

import ksn.model.shape.Shape

object TestHelper {

    fun convertTextList(shape: Shape): List<String> {
        return shape.toAsciiMatrix().joinToString { it.value }
    }

    fun convertTextList(shapes: List<Shape>, right: Int, bottom: Int): String {
        val matrix: Matrix<AsciiChar> = Matrix.init(right + 1, bottom + 1, AsciiChar.Space)
        val ascii = Ascii(matrix)
        ascii.mergeToMatrix(shapes)
        return ascii.matrix.joinToString { it.value }.joinToString("\n")
    }
}
