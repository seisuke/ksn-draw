package rtree

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


/**
 * A geometrical region that represents an Entry spatially. It is recommended
 * that implementations of this interface implement equals() and hashCode()
 * appropriately that entry equality checks work as expected.
 */
interface Geometry {
    /**
     * <p>
     * Returns the distance to the given {@link Rectangle}. For a {@link Rectangle}
     * this might be Euclidean distance but for an EPSG4326 lat-long Rectangle might
     * be great-circle distance. The distance function should satisfy the following
     * properties:
     * </p>
     *
     * <p>
     * <code>distance(r) &gt;= 0</code>
     * </p>
     *
     * <p>
     * <code>if r1 contains r2 then distance(r1)&lt;=distance(r2)</code>
     * </p>
     *
     *
     * @param r rectangle to measure distance to
     * @return distance to the rectangle r from the geometry
     */
    fun distance(r: Rectangle): Double

    /**
     * Returns the minimum bounding rectangle of this geometry.
     *
     * @return minimum bounding rectangle
     */
    fun mbr(): Rectangle
    fun intersects(r: Rectangle): Boolean
}

interface HasGeometry {
    fun geometry(): Geometry
}

data class Rectangle(
    val x1: Int,
    val y1: Int,
    val x2: Int,
    val y2: Int,
) : Geometry, HasGeometry {

    override fun intersects(r: Rectangle): Boolean = x1 <= r.x2 && r.x1 <= x2 && y1 <= r.y2 && r.y1 <= y2

    override fun distance(r: Rectangle): Double {
        return if (intersects(r)) {
            0.0
        } else {
            val xDistance = axisDistance(x1, x2, r.x1, r.x2)
            val yDistance = axisDistance(y1, y2, r.y1, r.y2)
            sqrt((xDistance * xDistance + yDistance * yDistance).toDouble())
        }
    }

    override fun mbr(): Rectangle = this

    override fun geometry(): Geometry = this

    fun add(other: Rectangle) = Rectangle(
        min(x1, other.x1),
        min(y1, other.y1),
        max(x2, other.x2),
        max(y2, other.y2)
    )

    fun contains(x: Int, y: Int) = x in x1..x2 && y in y1..y2

    fun area() = (x2 - x1) * (y2 - y1)

    fun intersectionArea(r: Rectangle): Int = if (!intersects(r)) {
        0
    } else {
        Rectangle(
            max(x1, r.x1),
            max(y1, r.y1),
            min(x2, r.x2),
            min(y2, r.y2)
        ).area()
    }

    fun perimeter(): Int = 2 * (x2 - x1) + 2 * (y2 - y1)

    private fun axisDistance(a1: Int, a2: Int, b1: Int, b2: Int): Int = if (a1 < b2) {
        b1 - a2
    } else {
        a1 - b2
    }

}

data class Point(
    val x: Int,
    val y: Int
) : Geometry, HasGeometry {
    private val rectangle = Rectangle(x, y, x, y)
    override fun distance(r: Rectangle) = rectangle.distance(r)
    override fun mbr() = rectangle.mbr()
    override fun intersects(r: Rectangle) = rectangle.intersects(r)
    override fun geometry() = rectangle.geometry()
}

/**
 *
 * Not thread safe.
 *
 * @param <T> list type
 */
class ListPair<T : HasGeometry>(list1: List<T>, list2: List<T>) {
    val group1: Group<T> = Group(list1)
    val group2: Group<T> = Group(list2)
    val marginSum: Int = group1.geometry().mbr().perimeter() + group2.geometry().mbr().perimeter()

    // these non-final variable mean that this class is not thread-safe
    // because access to them is not synchronized
    private var areaSum = -1

    fun areaSum(): Int {
        if (areaSum == -1) {
            areaSum = group1.geometry().mbr().area() + group2.geometry().mbr().area()
        }
        return areaSum
    }
}

class Group<T : HasGeometry>(
    val list: List<T>
) : HasGeometry {
    override fun geometry() = list.mbr()
}

fun Collection<HasGeometry>.mbr(): Rectangle {
    var minX1 = Int.MAX_VALUE
    var minY1 = Int.MAX_VALUE
    var maxX2 = Int.MIN_VALUE
    var maxY2 = Int.MIN_VALUE
    this.map {
        it.geometry().mbr()
    }.forEach { rectangle ->
        if (rectangle.x1 < minX1) minX1 = rectangle.x1
        if (rectangle.y1 < minY1) minY1 = rectangle.y1
        if (rectangle.x2 > maxX2) maxX2 = rectangle.x2
        if (rectangle.y2 > maxY2) maxY2 = rectangle.y2
    }

    return Rectangle(minX1, minY1, maxX2, maxY2)
}

