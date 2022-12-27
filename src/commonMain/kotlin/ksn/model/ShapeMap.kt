package ksn.model

import ksn.model.shape.Shape

/**
 * OrderChangeableLinkedHashMap
 */
class ShapeMap(
    val map: MutableMap<Long, Shape> = mutableMapOf(),
    val list: MutableList<Shape> = mutableListOf()
) : MutableMap<Long, Shape> by map {

    override val values: MutableCollection<Shape>
        get() = list

    fun add(key: Long, value: Shape, index: Int) {
        val oldValue = map.put(key, value)
        val actualIndex = if (oldValue != null) {
            val oldIndex = list.indexOf(oldValue)
            list.remove(oldValue)
            if (oldIndex < index) {
                index - 1
            } else {
                index
            }
        } else {
            index
        }
        list.add(actualIndex, value)
    }

    fun update(key: Long, update: (Shape) -> Shape?) {
        val oldValue = map[key] ?: return
        val newValue = update(oldValue) ?: return
        if (oldValue == newValue) {
            return
        }
        val index = list.indexOf(oldValue)
        list.removeAt(index)
        list.add(index, newValue)
        map[key] = newValue
    }

    fun subMap(keys: Set<Long>): Map<Long, Shape> {
        return keys.mapNotNull { key ->
            val value = map[key] ?: return@mapNotNull null
            key to value
        }.toMap()
    }

    inline fun <reified T : Shape> updateAllInstance(noinline update: (Pair<Long, T>) -> T?) {
        this.forEach { (id, shape) ->
            if (shape is T) {
                val newInstance = update(id to shape)
                this.update(id) { newInstance }
            }
        }
    }

    fun swap(indexA: Int, indexB: Int) {
        val tmp = list[indexA]
        list[indexA] = list[indexB]
        list[indexB] = tmp
    }

    fun swap(keyA: Long, keyB: Long): Boolean {
        val valueA = map[keyA] ?: return false
        val valueB = map[keyB] ?: return false
        val indexA = list.indexOf(valueA)
        val indexB = list.indexOf(valueB)
        swap(indexA, indexB)
        return true
    }

    override fun put(key: Long, value: Shape): Shape? {
        val oldValue = map[key]
        if (oldValue != null) {
            list.remove(value)
        }
        list.add(value)
        map[key] = value
        return oldValue
    }

    //TODO putAll()

    override fun remove(key: Long): Shape? {
        val oldValue = map.remove(key)
        if (oldValue != null) {
            list.remove(oldValue)
        }
        return oldValue
    }

    override fun clear() {
        map.clear()
        list.clear()
    }

    fun clone() : ShapeMap = ShapeMap(
        map.toMutableMap(),
        list.toMutableList()
    )
}

