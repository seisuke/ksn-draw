package ksn.update

import elm.None
import elm.Pure
import elm.plus
import ksn.model.Point
import ksn.model.RTreeEntry
import ksn.model.Tool
import ksn.model.minus
import ksn.model.shape.Shape
import ksn.toKsnLine
import ksn.toKsnRect
import ksn.toRTreeRectangle
import rtree.Entry
import rtree.Rectangle
import rtree.Point as RTreePoint

data class IntDragStatus(
    val start: Point,
    val end: Point,
) {
    data class DragStart(val dragStatus: IntDragStatus): Msg
    data class Drag(val dragStatus: IntDragStatus): Msg
    data class DragEnd(val dragStatus: IntDragStatus): Msg

    val isNotEmpty = start.x != end.x || start.y != end.y

    companion object {

        fun handleDragStart(
            model: AppModel,
            msg: DragStart
        ): Pure<AppModel> {
            if (model.selectShapeIdList.isEmpty()) {
                return model + None
            }
            val point = msg.dragStatus.start
            val shapeIdList = model.rtree.search(
                RTreePoint(
                    point.x,
                    point.y
                )
            ).map { it.value }

            // TODO fix intersect to imporove performance
            return if (shapeIdList.intersect(model.selectShapeIdList).isNotEmpty()) {
                model.copy(
                    tool = Tool.Select(true)
                ) + None
            } else {
                model + None
            }
        }

        fun handleDrag(
            model: AppModel,
            msg: Drag
        ): Pure<AppModel> {
            return if (model.tool is Tool.Select && model.tool.moving)  {
                val drag = msg.dragStatus.end - msg.dragStatus.start
                model.copy(
                    drag = drag,
                ) + None
            } else {
                model + None
            }
        }

        fun handleDragEnd(
            model: AppModel,
            msg: DragEnd
        ): Pure<AppModel> {
            return when (model.tool) {
                is Tool.Rect -> {
                    val id = model.maxId.getAndIncrement()
                    val rect = msg.dragStatus.toKsnRect(id)
                    model.returnUpdateModel(rect) + None
                }
                is Tool.Line -> {
                    val id = model.maxId.getAndIncrement()
                    val line = msg.dragStatus.toKsnLine(id)
                    model.returnUpdateModel(line) + None
                }
                is Tool.Select -> if (model.tool.moving) {

                    val entries1 = mutableListOf<Entry<Long, Rectangle>>()
                    val entries2 = mutableListOf<Entry<Long, Rectangle>>()

                    val shapes = model.shapes.map { shape ->
                        if (model.selectShapeIdList.contains(shape.id)) {
                            val drag = msg.dragStatus.end - msg.dragStatus.start
                            entries1.add(
                                RTreeEntry(shape.id, shape.toRTreeRectangle())
                            )
                            val newShape = shape.translate(drag)
                            entries2.add(
                                RTreeEntry(newShape.id, newShape.toRTreeRectangle())
                            )
                            newShape
                        } else {
                            shape
                        }
                    }

                    val rtree = model.rtree.delete(entries1).add(entries2)

                    model.copy(
                        tool = Tool.Select(),
                        shapes = shapes,
                        rtree = rtree,
                        drag = Point.Zero
                    ) + None

                } else {
                    val rTreeRect = msg.dragStatus.toRTreeRectangle()
                    val result = model.rtree.search(rTreeRect).toList()
                    val selectedIdList = result.map { it.value }
                    model.copy(
                        tool = Tool.Select(),
                        selectShapeIdList = selectedIdList,
                        drag = Point.Zero
                    ) + None
                }
                else -> model + None
            }
        }

        private fun AppModel.returnUpdateModel(shape: Shape) = if (shape.isEmpty) {
            this
        } else {
            val shapes = this.addShape(shape)
            val rtree = this.rtree.add(
                RTreeEntry(
                    shape.id,
                    shape.toRTreeRectangle()
                )
            )
            this.copy(shapes = shapes, rtree = rtree, drag = Point.Zero)
        }
    }
}
