package ksn.update

import kotlinx.atomicfu.AtomicLong
import ksn.model.Tool
import ksn.model.shape.Shape
import org.jetbrains.skia.Typeface
import rtree.RTree
import rtree.Rectangle

data class AppModel(
    val title: String,
    val tool: Tool,
    val maxId: AtomicLong,
    val shapes: List<Shape> = emptyList(),
    val selectShapeIdList: List<Long> = emptyList(),
    val rtree: RTree<Shape, Rectangle> = RTree.create(
        emptyList()
    ),
    val typeface: Typeface? = null,
) {

    data class CurrentTool(val tool: Tool): Msg()
    object StartLoadFont: Msg()
    data class LoadFontResult(val typeface: Typeface): Msg()

    object LoadFont: Cmd()

    /**
     * Mutable method. Returns list of [AppModel.shapes] plus [shape]
     */
    fun addShape(shape: Shape): List<Shape> {
        return shapes + shape
    }

    fun selectedShapes(): List<Shape> {
        if (selectShapeIdList.isEmpty()) {
            return emptyList()
        }

        return shapes.filter { shape ->
            selectShapeIdList.contains(shape.id)
        }
    }
}
