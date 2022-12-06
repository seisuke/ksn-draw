package ksn.ascii

import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import ksn.model.shape.TextBox

class Ascii(
    val matrix: Matrix<AsciiChar>
) {
    fun render(
        transform: (AsciiChar) -> String
    ): List<String> = matrix.joinToString(transform = transform)

    fun mergeToMatrix(shapes: List<Shape>) {
        shapes.forEach { shape ->
            val partAscii = when (shape) {
                is Rect -> shape.toAsciiMatrix()
                is Line -> shape.toAsciiMatrix()
                is TextBox -> shape.toAsciiMatrix()
            }

            matrix.merge(partAscii, shape.left, shape.top)
        }
    }

    fun Matrix<AsciiChar>.merge(other: Matrix<AsciiChar>, offsetX: Int, offsetY: Int) {
        other.withPoint().forEach { (otherX, otherY, value) ->
            if (value == AsciiChar.Transparent) {
                return@forEach
            }
            val x = otherX + offsetX
            val y = otherY + offsetY
            if (x < 0 || y < 0) {
                return@forEach
            }
            if (x >= this.width || y >= this.height ) {
                return@forEach
            }
            val oldValue = get(x, y)
            set(x, y, oldValue + value)
        }
    }

}

