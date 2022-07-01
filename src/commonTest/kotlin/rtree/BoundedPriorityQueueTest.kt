package rtree

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class BoundedPriorityQueueTest {

    private fun create(
        maxSize: Int,
        comparator: Comparator<Int>
    ): BoundedPriorityQueue<Int> = BoundedPriorityQueue(
        maxSize,
        comparator
    )

    @Test
    fun emptyQueueAsListIsEmpty() {
        val queue: BoundedPriorityQueue<Int> = create(2, comparator)
        assertTrue(queue.asOrderedList().isEmpty())
    }

    @Test
    fun threeItemsReturnsTwoItemsWhenMaxIsOneInputOrderIncreasing() {
        val queue = create(2, comparator)
        queue.add(1)
        queue.add(2)
        queue.add(3)
        assertEquals(listOf(1, 2), queue.asOrderedList())
    }

    @Test
    fun threeItemsReturnsTwoItemsWhenMaxIsOneInputOrderFlipped() {
        val queue = create(2, comparator)
        queue.add(2)
        queue.add(3)
        queue.add(1)
        assertEquals(listOf(1, 2), queue.asOrderedList())
    }

    @Test
    fun threeItemsReturnsThreeItemsWhenMaxIsOneInputOrderMixed() {
        val queue = create(10, comparator)
        queue.add(3)
        queue.add(1)
        queue.add(2)
        assertEquals(listOf(1, 2, 3), queue.asOrderedList())
    }

    companion object {
        private val comparator: Comparator<Int> = Comparator { o1, o2 -> o1.compareTo(o2) }
    }
}
