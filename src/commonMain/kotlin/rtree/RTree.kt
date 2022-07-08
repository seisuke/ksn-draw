package rtree

import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Immutable in-memory 2D R-Tree with configurable splitter heuristic.
 *
 * @param <T> the entry value type
 * @param <S> the entry geometry type
 */
class RTree<T, S : Geometry> private constructor(
    private val root: Node<T, S>?,
    private val size: Int,
    private val context: Context<T, S>
) {

    /**
     * The tree is scanned for depth and the depth returned. This involves recursing
     * down to the leaf level of the tree to get the current depth. Should be
     * `log(n)` in complexity.
     *
     * @return depth of the R-tree
     */
    fun calculateDepth(): Int = calculateDepth(root)

    /**
     * Returns an immutable copy of the RTree with the addition of given entry.
     *
     * @param entry
     * item to add to the R-tree.
     * @return a new immutable R-tree including the new entry
     */
    fun add(entry: Entry<T, S>): RTree<T, S> {
        return if (root != null) {
            val nodeList: List<Node<T, S>> = root.add(entry)
            val node = if (nodeList.size == 1) {
                nodeList.first()
            } else {
                context.factory.createNonLeaf(nodeList, context)
            }
            RTree(node, size + 1, context)
        } else {
            val node: Leaf<T, S> = context.factory.createLeaf(listOf(entry), context)
            RTree(node, size + 1, context)
        }
    }

    /**
     * Returns an immutable RTree with the current entries and the additional
     * entries supplied as a parameter.
     *
     * @param entries
     * entries to add
     * @return R-tree with entries added
     */
    fun add(entries: Iterable<Entry<T, S>>): RTree<T, S> = entries.fold(this) { tree, entry ->
        tree.add(entry)
    }

    /**
     * Returns a new R-tree with the given entries deleted. If `all` is
     * false deletes only one if exists. If `all` is true deletes all
     * matching entries.
     *
     * @param entries
     * entries to delete
     * @param all
     * if false deletes one if exists else deletes all
     * @return R-tree with entries deleted
     */
    fun delete(entries: Iterable<Entry<T, S>>, all: Boolean): RTree<T, S> =
        entries.fold(this) { tree, entry ->
            tree.delete(entry, all)
        }

    /**
     * Returns a new R-tree with the given entries deleted but only one matching
     * occurence of each entry is deleted.
     *
     * @param entries
     * entries to delete
     * @return R-tree with entries deleted up to one matching occurence per entry
     */
    fun delete(entries: Iterable<Entry<T, S>>): RTree<T, S> = entries.fold(this) { tree, entry ->
        tree.delete(entry)
    }

    /**
     * Deletes one entry if it exists, returning an immutable copy of the RTree
     * without that entry. If multiple copies of the entry are in the R-tree only
     * one will be deleted. The entry must match on both value and geometry to be
     * deleted.
     *
     * @param entry the [Entry] to be deleted
     * @return a new immutable R-tree without one instance of the specified entry
     */
    fun delete(entry: Entry<T, S>, all: Boolean = false): RTree<T, S> = if (root != null) {
        val nodeAndEntries: NodeAndEntries<T, S> = root.delete(entry, all)
        if (root == nodeAndEntries.node) {
            this
        } else {
            RTree(
                nodeAndEntries.node,
                size - nodeAndEntries.countDeleted - nodeAndEntries.entries.size,
                context
            ).add(nodeAndEntries.entries)
        }
    } else {
        this
    }

    /**
     * Returns an [Iterable] of [Entry] that satisfy the given
     * condition. Note that this method is well-behaved only if:
     *
     * `condition(g)` is true for [Geometry] g implies
     * `condition(r)` is true for the minimum bounding rectangles of the
     * ancestor nodes.
     *
     * `distance(g) < D` is an example of such a condition.
     *
     * @param condition
     * return Entries whose geometry satisfies the given condition
     * @return sequence of matching entries
     */
    fun search(condition: (Geometry) -> Boolean): Iterable<Entry<T, S>> {
        return if (root != null) {
            Search.search(root, condition)
        } else {
            emptyList()
        }
    }

    /**
     * Returns an [Iterable] sequence of all [Entry]s in the R-tree
     * whose minimum bounding rectangle intersects with the given rectangle.
     *
     * @param r
     * rectangle to check intersection with the entry mbr
     * @return entries that intersect with the rectangle r
     */
    fun search(r: Rectangle): Iterable<Entry<T, S>> {
        return search(intersects(r))
    }

    /**
     * Returns an [Iterable] sequence of all [Entry]s in the R-tree
     * whose minimum bounding rectangle intersects with the given point.
     *
     * @param p
     * point to check intersection with the entry mbr
     * @return entries that intersect with the point p
     */
    fun search(p: Point): Iterable<Entry<T, S>> {
        return search(p.mbr())
    }

    /**
     * Returns the intersections with the the given (arbitrary) geometry using an
     * intersection function to filter the search results returned from a search of
     * the mbr of `g`.
     *
     * @param <R>
     * type of geometry being searched for intersection with
     * @param g
     * geometry being searched for intersection with
     * @param intersects
     * function to determine if the two geometries intersect
     * @return a sequence of entries that intersect with g
     */
    fun <R : Geometry> search(
        g: R,
        intersects: (S, R) -> Boolean
    ): Iterable<Entry<T, S>> {
        return search(g.mbr()).filter { entry ->
            intersects(entry.geometry(), g)
        }.asIterable()
    }

    /**
     * Returns an [Iterable] sequence of all [Entry]s in the R-tree
     * whose minimum bounding rectangles are strictly less than maxDistance from the
     * given rectangle.
     *
     * @param r
     * rectangle to measure distance from
     * @param maxDistance
     * entries returned must be within this distance from rectangle r
     * @return the sequence of matching entries
     */
    fun search(r: Rectangle, maxDistance: Double): Iterable<Entry<T, S>> {
        return search { geometry ->
            geometry.distance(r) < maxDistance
        }
    }

    /**
     * Returns an [Iterable] sequence of all [Entry]s in the R-tree
     * whose minimum bounding rectangles are within maxDistance from the given
     * point.
     *
     * @param p
     * point to measure distance from
     * @param maxDistance
     * entries returned must be within this distance from point p
     * @return the sequence of matching entries
     */
    fun search(p: Point, maxDistance: Double): Iterable<Entry<T, S>> {
        return search(p.mbr(), maxDistance)
    }

    /**
     * Returns the nearest k entries (k=maxCount) to the given rectangle where the
     * entries are strictly less than a given maximum distance from the rectangle.
     *
     * @param r
     * rectangle
     * @param maxDistance
     * max distance of returned entries from the rectangle
     * @param maxCount
     * max number of entries to return
     * @return nearest entries to maxCount, in ascending order of distance
     */
    fun nearest(r: Rectangle, maxDistance: Double, maxCount: Int): Iterable<Entry<T, S>> {
        val q: BoundedPriorityQueue<Entry<T, S>> = BoundedPriorityQueue(
            maxCount,
            ascendingDistance(r)
        )

        for (entry in search(r, maxDistance)) {
            q.add(entry)
        }
        return q.asOrderedList()
    }

    /**
     * Returns the nearest k entries (k=maxCount) to the given point where the
     * entries are strictly less than a given maximum distance from the point.
     *
     * @param p
     * point
     * @param maxDistance
     * max distance of returned entries from the point
     * @param maxCount
     * max number of entries to return
     * @return nearest entries to maxCount, in ascending order of distance
     */
    fun nearest(p: Point, maxDistance: Double, maxCount: Int): Iterable<Entry<T, S>> {
        return nearest(p.mbr(), maxDistance, maxCount)
    }

    /**
     * Returns all entries in the tree as an [Iterable] sequence.
     *
     * @return all entries in the R-tree
     */
    fun entries(): Iterable<Entry<T, S>> {
        return search(ALWAYS_TRUE)
    }

    /**
     * If the RTree has no entries returns null
     * otherwise returns the minimum bounding rectangle of all entries in the RTree.
     *
     * @return minimum bounding rectangle of all entries in RTree
     */
    fun mbr(): Rectangle? = root?.geometry()?.mbr()

    /**
     * Returns true if and only if the R-tree is empty of entries.
     *
     * @return is R-tree empty
     */
    fun isEmpty(): Boolean = size == 0

    /**
     * Returns a predicate function that indicates if [Geometry] intersects
     * with a given rectangle.
     *
     * @param r the rectangle to check intersection with
     * @return whether the geometry and the rectangle intersect
     */
    fun intersects(r: Rectangle): (Geometry) -> Boolean = {
        it.intersects(r)
    }

    /**
     * Returns a human readable form of the RTree. Here's an example:
     *
     * <pre>
     * mbr=Rectangle [x1=10.0, y1=4.0, x2=62.0, y2=85.0]
     * mbr=Rectangle [x1=28.0, y1=4.0, x2=34.0, y2=85.0]
     * entry=Entry [value=2, geometry=Point [x=29.0, y=4.0]]
     * entry=Entry [value=1, geometry=Point [x=28.0, y=19.0]]
     * entry=Entry [value=4, geometry=Point [x=34.0, y=85.0]]
     * mbr=Rectangle [x1=10.0, y1=45.0, x2=62.0, y2=63.0]
     * entry=Entry [value=5, geometry=Point [x=62.0, y=45.0]]
     * entry=Entry [value=3, geometry=Point [x=10.0, y=63.0]]
     * </pre>
     *
     * @return a string representation of the RTree
     */
    fun asString(): String {
        return if (root == null) {
            ""
        } else {
            asString(root, "")
        }
    }

    private fun asString(node: Node<T, S>, margin: String): String {
        val s = StringBuilder()
        s.append(margin)
        s.append("mbr=")
        s.append(node.geometry())
        s.append('\n')
        when (node) {
            is NonLeaf -> node.children.forEach { child ->
                s.append(asString(child, margin + marginIncrement))
            }
            is Leaf -> node.entries.forEach { entry ->
                s.append(margin)
                s.append(marginIncrement)
                s.append("entry=")
                s.append(entry)
                s.append('\n')
            }
        }
        return s.toString()
    }

    private fun calculateDepth(root: Node<T, S>?): Int {
        return if (root == null) {
            0
        } else {
            calculateDepth(root, 0)
        }
    }

    private tailrec fun calculateDepth(node: Node<T, S>, depth: Int): Int {
        return when (node) {
            is Leaf -> depth + 1
            is NonLeaf -> calculateDepth(
                node.child(0),
                depth + 1
            )
        }
    }

    private fun calculateMaxView(tree: RTree<T, S>): Rectangle {
        return tree
            .entries()
            .fold(Rectangle(0, 0, 0, 0)) { rectangle, entry ->
                rectangle.add(entry.geometry().mbr())
            }
    }

    private fun ascendingDistance(
        r: Rectangle
    ): Comparator<Entry<T, S>> = Comparator { a, b ->
        val aDistance = a.geometry().distance(r)
        val bDistance = b.geometry().distance(r)
        aDistance.compareTo(bDistance)
    }

    companion object {

        fun <T, S : Geometry> create(
            entries: List<Entry<T, S>>,
            maxChildren: Int = MAX_CHILDREN_DEFAULT_GUTTMAN,
            minChildren: Int = round(maxChildren * DEFAULT_FILLING_FACTOR).toInt(),
            splitter: Splitter = SplitterQuadratic(),
            selector: Selector<T, S> = SelectorMinimalAreaIncrease(),
            loadingFactor: Double = DEFAULT_LOADING_FACTOR,
        ): RTree<T, S> = Builder(
            entries,
            maxChildren,
            minChildren,
            splitter,
            selector,
            loadingFactor
        ).create()

        /**
         * According to http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf (R*-tree paper),
         * best filling ratio is 0.4 for both quadratic split and R*-tree split.
         */
        private const val DEFAULT_FILLING_FACTOR = 0.4
        private const val DEFAULT_LOADING_FACTOR = 0.7

        /**
         * Benchmarks show that this is a good choice for up to O(10,000) entries when
         * using Quadratic splitter (Guttman).
         */
        private const val MAX_CHILDREN_DEFAULT_GUTTMAN = 4

        /**
         * Returns the always true predicate. See [RTree.entries] for example use.
         */
        private val ALWAYS_TRUE: (Geometry) -> Boolean = { true }
        private const val marginIncrement = "  "
    }

    internal class FactoryDefault<T, S : Geometry> : Factory<T, S> {

        override fun createLeaf(entries: List<Entry<T, S>>, context: Context<T, S>): Leaf<T, S> {
            return LeafDefault(entries, context)
        }

        override fun createNonLeaf(children: List<Node<T, S>>, context: Context<T, S>): NonLeaf<T, S> {
            return NonLeafDefault(children, context)
        }
    }

    /**
     * RTree Builder.
     */
    data class Builder<T, S : Geometry>(
        val entries: List<Entry<T, S>>,
        val maxChildren: Int,
        val minChildren: Int,
        val splitter: Splitter,
        val selector: Selector<T, S>,
        val loadingFactor: Double,
    ) {

        /**
         * Create an RTree by bulk loading, using the STR method.
         * STR: a simple and efficient algorithm for R-tree packing
         * http://ieeexplore.ieee.org/abstract/document/582015/
         *
         * Note: this method mutates the input entries, the internal order of the List
         * may be changed.
         *
         * @return a loaded RTree
         */
        fun create(): RTree<T, S> {
            val context: Context<T, S> = Context(
                minChildren,
                maxChildren,
                selector,
                splitter,
                FactoryDefault()
            )
            return packingSTR(
                entries.eitherLeft(),
                entries.size,
                { context.factory.createLeaf(it, context) },
                { context.factory.createNonLeaf(it, context) },
                context
            )
        }

        private tailrec fun packingSTR(
            eitherObjects: Either<List<Entry<T, S>>, List<Node<T, S>>>,
            size: Int,
            entriesToLeaf: (List<Entry<T, S>>) -> Node<T, S>,
            nodeToNonLeaf: (List<Node<T, S>>) -> Node<T, S>,
            context: Context<T, S>
        ): RTree<T, S> {
            val objectsSize = eitherObjects.fold(List<*>::size, List<*>::size)
            val capacity = round(maxChildren * loadingFactor).toInt()
            val nodeCount = ceil(1.0 * objectsSize / capacity).toInt()
            if (nodeCount == 0) {
                return RTree(null, size, context)
            } else if (nodeCount == 1) {
                val root = eitherObjects.fold(
                    entriesToLeaf,
                    nodeToNonLeaf
                )
                return RTree(root, size, context)
            }
            val nodes = eitherObjects.fold(
                { it.toDepth2List(capacity, nodeCount).map(entriesToLeaf) },
                { it.toDepth2List(capacity, nodeCount).map(nodeToNonLeaf) }
            )
            return packingSTR(
                nodes.eitherRight(),
                size,
                entriesToLeaf,
                nodeToNonLeaf,
                context
            )
        }

        private fun <R : HasGeometry> List<R>.toDepth2List(capacity: Int, nodeCount: Int): List<List<R>> {
            val sliceCapacity = ceil(sqrt(nodeCount.toDouble())).toInt() * capacity
            val sliceCount = ceil(1.0 * this.size / sliceCapacity).toInt()
            return this.sortedWith(MidComparator(0.toShort()))
                .asSequence()
                .chunked(sliceCapacity) {
                    it.sortedWith(MidComparator(1.toShort()))
                }
                .take(sliceCount)
                .flatMap { slice ->
                    slice.chunked(capacity)
                }
                .toList()
        }

        private class MidComparator(
            // leave space for multiple dimensions, 0 for x, 1 for y,
            private val dimension: Short
        ) : Comparator<HasGeometry> {

            override fun compare(a: HasGeometry, b: HasGeometry): Int {
                return compareValues(mid(a), mid(b))
            }

            private fun mid(o: HasGeometry): Double {
                val mbr: Rectangle = o.geometry().mbr()
                return if (dimension.toInt() == 0) {
                    (mbr.x1 + mbr.x2) / 2
                } else {
                    (mbr.y1 + mbr.y2) / 2
                }.toDouble()
            }
        }
    }
}
