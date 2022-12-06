package ksn.ascii

class Matrix<T> private constructor(
    val width: Int,
    val height: Int,
    initialValue: List<T>
) {
    var initial: T? = null

    private val length = width * height

    private val array: MutableList<T> = if (initialValue.size != length) {
        throw IllegalArgumentException("initialValue's size must be same with width * height")
    } else {
        initialValue.toMutableList()
    }

    fun get(x: Int, y: Int): T = array[index(x, y)]

    fun set(x: Int, y: Int, value: T) {
        array[index(x, y)] = value
    }

    fun clear() {
        val a = initial
        if (a != null) {
            array.fill(a)
        }
    }

    fun joinToString(
        separator: String = "",
        transform: (T) -> String
    ): List<String> =
        array.chunked(width)
            .map { row ->
                row.joinToString(
                    separator,
                    transform = transform
                )
            }

    fun setValues(x: Int, y: Int, value: List<T>) {
        array.addAll(index(x, y), value)
    }

    fun withPoint() = iterator {
        var x = 0
        var y = 0
        array.forEach {
            yield(Triple(x, y, it))
            x += 1
            if (x >= width) {
                x = 0
                y += 1
            }
        }
    }

    private fun index(x: Int, y: Int) = x + y * width

    companion object {
        fun <T> init(
            width: Int,
            height: Int,
            initial: T
        ): Matrix<T> {
            val matrix = Matrix(
                width,
                height,
                List(width * height) { initial }
            )
            matrix.initial = initial
            return matrix
        }

    }
}

