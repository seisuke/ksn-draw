package rtree

expect class BoundedPriorityQueue<T>(
    maxSize: Int,
    comparator: Comparator<T>
) {
    fun add(element: T)

    fun asOrderedList(): List<T>
}
