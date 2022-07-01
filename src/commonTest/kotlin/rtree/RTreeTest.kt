package rtree

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class RTreeTest {

    @Test
    fun pointSearch() {
        val entry1 = point(1)
        val tree: RTree<Int, Point> = RTree.create(
            listOf(entry1)
        )
        val result = tree.search(Point(1,1)).toList().first()
        assertEquals(entry1, result)
    }

    @Test
    fun pointSearchWithDistance() {
        val entry1 = point(1)
        val tree: RTree<Int, Point> = RTree.create(
            listOf(entry1)
        )
        val result = tree.search(Point(10,10), 100.0).toList()
        assertEquals(entry1, result.first())
    }

    @Test
    fun pointSearchOutOfDistance() {
        val entry1 = point(1)
        val tree: RTree<Int, Point> = RTree.create(
            listOf(entry1)
        )
        val result = tree.search(Point(10,10), 10.0).toList()
        assertTrue(result.isEmpty())
    }

    @Test
    fun nearestWithMaxCount() {
        val entries = (0..10).map { point(it) }.shuffled()
        val tree: RTree<Int, Point> = RTree.create(
            entries
        )
        val result = tree.nearest(Point(3,3), 10.0, 3).toList()
        assertEquals(result.size, 3)
    }

    @Test
    fun nearestWithMaxDistance() {
        val entries = (0..10).map { point(it) }.shuffled()
        val tree: RTree<Int, Point> = RTree.create(
            entries
        )
        val result = tree.nearest(Point(3,3), 2.0, 10).toList()
        assertEquals(result.size, 3)
    }

    @Test
    fun rectangleSearch() {
        val entries = listOf(
            rectangle(1, 0, 0, 1, 1),
            rectangle(2, 2, 2, 3, 3),
            rectangle(3, 0, 0, 5, 5),
        )

        val tree: RTree<Int, Rectangle> = RTree.create(
            entries
        )

        val result1 = tree.search(Rectangle(1, 1, 1, 1)).toList()
        assertEquals(result1.map { it.value() }, listOf(1, 3))

        val result2 = tree.search(Rectangle(4, 4, 7, 7)).toList()
        assertEquals(result2.map { it.value() }, listOf(3))

        val result3 = tree.search(Rectangle(0, 0, 5, 5)).toList()
        assertEquals(result3.map { it.value() }, listOf(1, 2, 3))
    }

    @Test
    fun asString() {
        val entries = (0..20).map { point(it) }
        val tree: RTree<Int, Point> = RTree.create(
            entries
        )

        val tree2 = tree.add(point(10)).add(point(11)).add(point(12))

        val tree3 = tree2.delete(entries.subList(0, 10))
        println(tree3.asString()) //TODO fix toString
    }

    @ExperimentalTime
    @Test
    fun performance() {
        val milliSeconds = measureTime {
            val entries = (0..100000).map { point(it) }
            val tree: RTree<Int, Point> = RTree.create(
                entries
            )
            val result = tree.nearest(Point(100, 100), 100.0, 30).toList()
            println(result)
        }.inWholeMilliseconds
        println(milliSeconds)
    }

    private fun point(n: Int): PointEntry {
        return PointEntry(n,Point(n, n))
    }

    private fun rectangle(
        n: Int,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
    ): RectangleEntry {
        return RectangleEntry(
            n,
            Rectangle(
                x1, y1, x2, y2
            )
        )
    }

    data class PointEntry(
        val v: Int,
        val p: Point,
    ): Entry<Int, Point> {
        override fun value() = v
        override fun geometry() = p
    }

    data class RectangleEntry(
        val v: Int,
        val rectangle: Rectangle,
    ): Entry<Int, Rectangle> {
        override fun value() = v
        override fun geometry() = rectangle
    }

}
