package ksn.ascii

import ksn.model.shape.Shape

class Ascii(
    val matrix: Matrix<AsciiChar>
) {
    fun render(
        transform: (AsciiChar) -> String
    ): List<String> = matrix.joinToString(transform = transform)

    fun mergeToMatrix(shapeList: List<Shape>) {
        shapeList.forEach { shape ->
            val partAscii = shape.toAsciiMatrix()
            matrix.merge(partAscii, shape.left, shape.top)
        }
    }

    fun Matrix<AsciiChar>.merge(other: Matrix<AsciiChar>, offsetX: Int, offsetY: Int) {
        other.withPoint().forEach { (otherX, otherY, asciiChar) ->
            val x = otherX + offsetX
            val y = otherY + offsetY
            if (x < 0 || y < 0) {
                return@forEach
            }
            if (x >= this.width || y >= this.height ) {
                return@forEach
            }

            // TODO fix problem how to display overlaped 2 width character
            when (asciiChar) {
                is AsciiChar.Transparent -> Unit
                is AsciiChar.Emoji -> {
                    set(x, y, asciiChar.copy(x = x, y = y))
                }
                else -> {
                    val oldValue = get(x, y)
                    set(x, y, oldValue + asciiChar)
                }
            }
        }
    }

}

