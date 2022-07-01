package rtree

internal object Search {

    fun <T, S : Geometry> search(
        node: Node<T, S>,
        condition: (Geometry) -> Boolean
    ): Iterable<Entry<T, S>> = SearchIterable(node, condition)

    internal class SearchIterable<T, S : Geometry>(
        private val node: Node<T, S>,
        private val condition: (Geometry) -> Boolean
    ) : Iterable<Entry<T, S>> {
        override fun iterator(): Iterator<Entry<T, S>> {
            return SearchIterator(node, condition)
        }
    }

    internal class SearchIterator<T, S : Geometry>(
        private val node: Node<T, S>,
        private val condition: (Geometry) -> Boolean
    ) : MutableIterator<Entry<T, S>> {
        private val stack: ArrayDeque<NodePosition<T, S>> = ArrayDeque<NodePosition<T, S>>().apply {
            add(NodePosition(node, 0))
        }
        private var next: Entry<T, S>? = null

        override fun hasNext(): Boolean {
            load()
            return next != null
        }

        override fun next(): Entry<T, S> {
            load()
            val value = next
            return if (value == null) {
                throw NoSuchElementException()
            } else {
                next = null
                value
            }
        }

        override fun remove() {
            TODO("Not yet implemented")
        }

        private fun load() {
            if (next == null) {
                next = search()
            }
        }

        private fun search(): Entry<T, S>? {
            while (stack.isNotEmpty()) {
                val nodePosition = stack.last()
                if (!nodePosition.hasRemaining()) {
                    searchAfterLastInNode()
                } else {
                    when (nodePosition.node) {
                        is NonLeaf -> searchNonLeaf(nodePosition, nodePosition.node)
                        is Leaf -> searchLeaf(nodePosition, nodePosition.node)?.let { findEntry ->
                            return findEntry
                        }
                    }
                }
            }
            return null
        }

        private fun searchAfterLastInNode() {
            stack.removeLast()
            if (!stack.isEmpty()) {
                val previous = stack.last()
                previous.position += 1
            }
        }

        private fun searchNonLeaf(
            nodePosition: NodePosition<T, S>,
            nonLeaf: NonLeaf<T, S>
        ) {
            val child = nonLeaf.child(nodePosition.position)
            if (condition(child.geometry())) {
                stack.add(NodePosition(child, 0))
            } else {
                nodePosition.position += 1
            }
        }

        private fun searchLeaf(
            nodePosition: NodePosition<T, S>,
            leaf: Leaf<T, S>
        ): Entry<T, S>? {
            var i: Int = nodePosition.position
            do {
                val entry = leaf.entry(i)
                if (condition(entry.geometry())) {
                    nodePosition.position = i + 1
                    return entry
                }
                i++
            } while (i < leaf.count())
            nodePosition.position = i
            return null
        }
    }
}
