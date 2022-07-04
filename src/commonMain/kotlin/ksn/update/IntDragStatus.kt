package ksn.update

import elm.None
import elm.Pure
import elm.plus
import ksn.model.Point
import ksn.model.Tool
import ksn.model.shape.Shape
import ksn.toKsnLine
import ksn.toKsnRect
import ksn.toRTreeRectangle
import rtree.Entry

data class IntDragStatus(
    val start: Point,
    val end: Point,
) {
    data class DragEnd(val dragStatus: IntDragStatus): Msg

    companion object {
        val Zero = IntDragStatus(
            Point(0, 0),
            Point(0, 0)
        )

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
                is Tool.Select -> {
                    val rTreeRect = msg.dragStatus.toRTreeRectangle()
                    val result = model.rtree.search(rTreeRect).toList()
                    val selectedIdList = result.map { it.value.id }
                    model.copy(selectShapeIdList = selectedIdList) + None
                }
                else -> model + None
            }
        }

        private fun AppModel.returnUpdateModel(shape: Shape) = if (shape.isEmpty) {
            this
        } else {
            val shapes = this.addShape(shape)
            val rtree = this.rtree.add(
                Entry(
                    shape,
                    shape.toRTreeRectangle()
                )
            )
            this.copy(shapes = shapes, rtree = rtree)
        }
    }
}
