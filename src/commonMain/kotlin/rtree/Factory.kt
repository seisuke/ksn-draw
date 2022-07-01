package rtree

interface Factory<T, GEOMETRY : Geometry> :
    LeafFactory<T, GEOMETRY>,
    NonLeafFactory<T, GEOMETRY>

interface LeafFactory<T, GEOMETRY : Geometry> {
    fun createLeaf(
        entries: List<Entry<T, GEOMETRY>>,
        context: Context<T, GEOMETRY>
    ): Leaf<T, GEOMETRY>
}

interface NonLeafFactory<T, GEOMETRY : Geometry> {
    fun createNonLeaf(
        children: List<Node<T, GEOMETRY>>,
        context: Context<T, GEOMETRY>
    ): NonLeaf<T, GEOMETRY>
}
