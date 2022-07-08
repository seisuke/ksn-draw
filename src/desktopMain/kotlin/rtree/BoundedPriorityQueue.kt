package rtree

import java.util.*

actual class BoundedPriorityQueue<T> actual constructor(
    private val maxSize: Int,
    private val comparator: Comparator<T>
) {
    private val queue = PriorityQueue(comparator.reversed())

    actual fun add(element: T) {
        if (queue.size >= maxSize) {
            val maxElement = queue.peek()
            if (comparator.compare(maxElement, element) < 1) {
                return
            } else {
                queue.poll()
            }
        }
        queue.add(element)
    }

    actual fun asOrderedList(): List<T> = queue.toList().sortedWith(comparator)

}

