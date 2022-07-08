package ksn.model

import rtree.Entry
import rtree.Geometry

data class RTreeEntry<T, GEOMETRY : Geometry>(
    override val value: T,
    private val geometry: GEOMETRY
) : Entry<T, GEOMETRY> {
    override fun geometry(): GEOMETRY = geometry
}
