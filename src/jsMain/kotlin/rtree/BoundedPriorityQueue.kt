package rtree

actual class BoundedPriorityQueue<T> actual constructor(
    val maxSize: Int,
    val comparator: Comparator<T>
) {

    actual fun add(element: T) {
    }

    actual fun asOrderedList(): List<T> {
        TODO("Not yet implemented")
    }

}
