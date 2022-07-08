package rtree

sealed interface Node<T, GEOMETRY : Geometry> : HasGeometry {
    fun add(entry: Entry<T, GEOMETRY>): List<Node<T, GEOMETRY>>
    fun delete(entry: Entry<T, GEOMETRY>, all: Boolean): NodeAndEntries<T, GEOMETRY>
    fun count(): Int
    val context: Context<T, GEOMETRY>
}

/**
 * Used for tracking deletions through recursive calls.
 */
data class NodeAndEntries<T, GEOMETRY : Geometry>(
    val node: Node<T, GEOMETRY>?,
    val entries: List<Entry<T, GEOMETRY>>,
    val countDeleted: Int
)

interface Entry<T, GEOMETRY : Geometry> : HasGeometry {
    val value: T
    override fun geometry(): GEOMETRY
}

interface Leaf<T, GEOMETRY : Geometry> : Node<T, GEOMETRY> {
    val entries: List<Entry<T, GEOMETRY>>

    /**
     * Returns the ith entry (0-based). This method should be preferred for
     * performance reasons when only one entry is required (in comparison to
     * `entries().get(i)`).
     *
     * @param i
     * 0-based index
     * @return ith entry
     */
    fun entry(i: Int): Entry<T, GEOMETRY>
}

interface NonLeaf<T, GEOMETRY : Geometry> : Node<T, GEOMETRY> {
    fun child(i: Int): Node<T, GEOMETRY>

    /**
     * Returns a list of children nodes. For accessing individual children the
     * child(int) method should be used to ensure good performance. To avoid
     * copying an existing list though this method can be used.
     *
     * @return list of children nodes
     */
    val children: List<Node<T, GEOMETRY>>
}

//Mutable, not thread-safe
internal class NodePosition<T, GEOMETRY : Geometry>(
    val node: Node<T, GEOMETRY>,
    var position: Int
) {
    fun hasRemaining(): Boolean = position != node.count()
}

class LeafDefault<T, GEOMETRY : Geometry>(
    override val entries: List<Entry<T, GEOMETRY>>,
    override val context: Context<T, GEOMETRY>
) : Leaf<T, GEOMETRY> {

    override fun geometry() = entries.mbr()

    override fun count() = entries.size

    override fun entry(i: Int) = entries[i]

    override fun add(entry: Entry<T, GEOMETRY>): List<Node<T, GEOMETRY>> {
        val entries2: List<Entry<T, GEOMETRY>> = entries + entry
        return if (entries2.size <= context.maxChildren) listOf(
            context.factory.createLeaf(entries2, context)
        ) else {
            val pair: ListPair<Entry<T, GEOMETRY>> = context.splitter.split(entries2, context.minChildren)
            makeLeaves(pair, context)
        }
    }

    override fun delete(entry: Entry<T, GEOMETRY>, all: Boolean): NodeAndEntries<T, GEOMETRY> {
        if (!entries.contains(entry)) {
            return NodeAndEntries(this, emptyList(), 0)
        } else {
            val entries2 = entries.toMutableList()
            entries2.remove(entry)
            var numDeleted = 1
            // keep deleting if all specified
            while (all && entries2.remove(entry)) numDeleted += 1

            return if (entries2.size >= context.minChildren) {
                val leaf = context.factory.createLeaf(entries2, context)
                NodeAndEntries(
                    leaf,
                    emptyList(),
                    numDeleted
                )
            } else {
                NodeAndEntries(
                    null,
                    entries2,
                    numDeleted
                )
            }
        }
    }

    private fun <T, GEOMETRY : Geometry> makeLeaves(
        pair: ListPair<Entry<T, GEOMETRY>>,
        context: Context<T, GEOMETRY>
    ): List<Node<T, GEOMETRY>> = listOf(
        context.factory.createLeaf(pair.group1.list, context),
        context.factory.createLeaf(pair.group2.list, context)
    )
}

class NonLeafDefault<T, GEOMETRY : Geometry>(
    override val children: List<Node<T, GEOMETRY>>,
    override val context: Context<T, GEOMETRY>,
) : NonLeaf<T, GEOMETRY> {

    init {
        require(children.isNotEmpty())
    }

    override fun geometry(): Geometry = children.mbr()

    override fun count(): Int = children.size

    override fun child(i: Int): Node<T, GEOMETRY> = children[i]

    override fun add(entry: Entry<T, GEOMETRY>): List<Node<T, GEOMETRY>> {
        val child: Node<T, GEOMETRY> = context.selector.select(entry.geometry().mbr(), children)
        val list = child.add(entry)
        val children2: List<Node<T, GEOMETRY>> = children.filter { it != child }.toMutableList().apply {
            addAll(list)
        }
        return if (children2.size <= context.maxChildren) {
            listOf(
                context.factory.createNonLeaf(children2, context)
            )
        } else {
            val pair: ListPair<Node<T, GEOMETRY>> = context.splitter.split(
                children2,
                context.minChildren
            )
            makeNonLeaves(pair, context)
        }
    }

    override fun delete(entry: Entry<T, GEOMETRY>, all: Boolean): NodeAndEntries<T, GEOMETRY> {
        // the result of performing a delete of the given entry from this node
        // will be that zero or more entries will be needed to be added back to
        // the root of the tree (because num entries of their node fell below
        // minChildren),
        // zero or more children will need to be removed from this node,
        // zero or more nodes to be added as children to this node(because
        // entries have been deleted from them and they still have enough
        // members to be active)
        val addTheseEntries: MutableList<Entry<T, GEOMETRY>> = mutableListOf()
        val removeTheseNodes: MutableList<Node<T, GEOMETRY>> = mutableListOf()
        val addTheseNodes: MutableList<Node<T, GEOMETRY>> = mutableListOf()
        var countDeleted = 0
        for (child in children) {
            if (this.geometry().intersects(child.geometry().mbr())) {
                val result = child.delete(entry, all)
                val resultNode = result.node
                if (resultNode != null) {
                    if (resultNode !== child) {
                        // deletion occurred and child is above minChildren so we update it
                        addTheseNodes.add(resultNode)
                        removeTheseNodes.add(child)
                        addTheseEntries.addAll(result.entries)
                        countDeleted += result.countDeleted
                        if (!all) break
                    }
                    // else nothing was deleted from that child
                } else {
                    // deletion occurred and brought child below minChildren so we redistribute its entries
                    removeTheseNodes.add(child)
                    addTheseEntries.addAll(result.entries)
                    countDeleted += result.countDeleted
                    if (!all) break
                }
            }
        }
        if (removeTheseNodes.isEmpty()) {
            return NodeAndEntries(this, emptyList(), 0)
        }

        val nodes: MutableList<Node<T, GEOMETRY>> = children.toMutableList().apply {
            removeAll(removeTheseNodes)
            addAll(addTheseNodes)
        }
        return if (nodes.isEmpty()) {
            NodeAndEntries(
                null,
                addTheseEntries,
                countDeleted
            )
        } else {
            val nonLeaf: NonLeaf<T, GEOMETRY> = this.context.factory.createNonLeaf(nodes, this.context)
            NodeAndEntries(
                nonLeaf,
                addTheseEntries,
                countDeleted
            )
        }
    }

    private fun <T, GEOMETRY : Geometry> makeNonLeaves(
        pair: ListPair<out Node<T, GEOMETRY>>,
        context: Context<T, GEOMETRY>
    ): List<Node<T, GEOMETRY>> = listOf(
        context.factory.createNonLeaf(pair.group1.list, context),
        context.factory.createNonLeaf(pair.group2.list, context)
    )
}


