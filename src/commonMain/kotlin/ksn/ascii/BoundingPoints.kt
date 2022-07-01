package ksn.ascii

import ksn.model.Point

sealed class BoundingPoints
data class Side(val value: Pair<Point, Point>): BoundingPoints()
data class Edge(val value: Triple<Point, Point, Point>): BoundingPoints()
