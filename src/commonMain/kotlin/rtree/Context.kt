package rtree

/**
 * Configures an RTree prior to instantiation of an [RTree].
 *
 * @constructor
 * @param minChildren minimum number of children per node (at least 1)
 * @param maxChildren max number of children per node (minimum is 3)
 * @param selector algorithm to select search path
 * @param splitter algorithm to split the children across two new nodes
 * @param factory node creation factory
 */
data class Context<T, GEOMETRY : Geometry>(
    val minChildren: Int,
    val maxChildren: Int,
    val selector: Selector<T, GEOMETRY>,
    val splitter: Splitter,
    val factory: Factory<T, GEOMETRY>,
) {
    init {
        require(minChildren >= 1)
        require(maxChildren > 2)
        require(minChildren < maxChildren)
    }
}

/**
 * The heuristic used on insert to select which node to add an Entry to.
 *
 */
interface Selector<T, GEOMETRY : Geometry> {
    /**
     * Returns the node from a list of nodes that an object with the given
     * geometry would be added to.
     *
     * @param geometry
     * geometry
     * @param nodes
     * nodes to select from
     * @return one of the given nodes
     */
    fun select(
        geometry: Geometry,
        nodes: List<Node<T, GEOMETRY>>
    ): Node<T, GEOMETRY>
}

interface Splitter {
    /**
     * Splits a list of items into two lists of at least minSize.
     *
     * @param items list of items to split
     * @param minSize min size of each list
     * @return two lists
     */
    fun <T : HasGeometry> split(items: List<T>, minSize: Int): ListPair<T>
}

/**
 * Uses minimal area increase to select a node from a list.
 *
 */
class SelectorMinimalAreaIncrease<T, S : Geometry> : Selector<T, S> {
    override fun select(
        geometry: Geometry,
        nodes: List<Node<T, S>>
    ): Node<T, S> {
        return nodes.minWithOrNull(areaIncreaseThenAreaComparator(geometry.mbr())) ?: nodes.first()
    }

    private fun areaIncreaseThenAreaComparator(
        rectangle: Rectangle
    ): Comparator<HasGeometry> = Comparator { g1, g2 ->
        val value = areaIncrease(rectangle, g1).compareTo(areaIncrease(rectangle, g2))
        if (value == 0) {
            area(rectangle, g1).compareTo(area(rectangle, g2))
        } else {
            value
        }
    }

    private fun area(rectangle: Rectangle, hasGeometry: HasGeometry): Int {
        return hasGeometry.geometry().mbr().add(rectangle).area()
    }

    private fun areaIncrease(rectangle: Rectangle, hasGeometry: HasGeometry): Int {
        val gPlusR = hasGeometry.geometry().mbr().add(rectangle)
        return gPlusR.area() - hasGeometry.geometry().mbr().area()
    }
}

/**
 * according to
 * http://en.wikipedia.org/wiki/R-tree#Splitting_an_overflowing_node
 */
class SplitterQuadratic : Splitter {

    override fun <T : HasGeometry> split(items: List<T>, minSize: Int): ListPair<T> {

        // find the worst combination pairwise in the list and use them to start
        // the two groups
        val (worstCombination1, worstCombination2) = worstCombination(items)

        // worst combination to have in the same node is now e1,e2.

        // establish a group around e1 and another group around e2
        val remaining: List<T> = items - listOf(
            worstCombination1,
            worstCombination2
        )
        val minGroupSize = items.size / 2

        // now add the remainder to the groups using least mbr area increase
        // except in the case where minimumSize would be contradicted
        val (group1Result, group2Result) = assignRemaining(
            listOf(worstCombination1),
            listOf(worstCombination2),
            remaining,
            minGroupSize
        )
        return ListPair(group1Result, group2Result)
    }

    private tailrec fun <T : HasGeometry> assignRemaining(
        group1: List<T>,
        group2: List<T>,
        remaining: List<T>,
        minGroupSize: Int
    ): Pair<List<T>, List<T>> = if (remaining.isEmpty()) {
        group1 to group2
    } else {
        val mbr1 = group1.mbr()
        val mbr2 = group2.mbr()
        val item1 = getBestCandidateForGroup(remaining, mbr1)
        val item2 = getBestCandidateForGroup(remaining, mbr2)
        val area1LessThanArea2 =
            item1.geometry().mbr().add(mbr1).area() <= item2.geometry().mbr().add(mbr2).area()
        if (
            area1LessThanArea2 && group2.size + remaining.size - 1 >= minGroupSize ||
            !area1LessThanArea2 && group1.size + remaining.size == minGroupSize
        ) {
            assignRemaining(
                group1 + item1,
                group2,
                remaining - item1,
                minGroupSize
            )
        } else {
            assignRemaining(
                group1,
                group2 + item2,
                remaining - item2,
                minGroupSize
            )
        }
    }

    private fun <T : HasGeometry> getBestCandidateForGroup(
        list: List<T>,
        groupMbr: Rectangle
    ): T {
        val minEntry = list.minByOrNull { entry ->
            groupMbr.add(entry.geometry().mbr()).area()
        } ?: list.first()
        return minEntry
    }

    private fun <T : HasGeometry> worstCombination(items: List<T>): Pair<T, T> {
        val mbrList: List<Pair<T, Rectangle>> = items.map {
            it to it.geometry().mbr()
        }
        val combination = mbrList.flatMap { mbr ->
            List(mbrList.size) { mbr }.zip(mbrList)
        }

        val maxCombination = combination.maxByOrNull { (a, b) ->
            a.second.add(b.second).area()
        }?.let { (a, b) ->
            a.first to b.first
        } ?: (items[0] to items[0])

        return maxCombination
    }
}
