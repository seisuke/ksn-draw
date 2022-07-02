package ksn.update

import androidx.compose.ui.geometry.Offset
import elm.None
import elm.Pure
import elm.plus
import ksn.model.Tool
import ksn.model.shape.Shape
import ksn.toKsnLine
import ksn.toKsnRect
import rtree.Entry
import rtree.Rectangle

data class DragStatus(
    val start: Offset,
    val end: Offset,
) {
    data class DragEnd(val dragStatus: DragStatus): Msg

    companion object {
        val Zero = DragStatus(
            Offset.Zero,
            Offset.Zero
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
                    val rect = msg.dragStatus.toKsnRect(0) //TODO fix 0
                    val result = model.rtree.search(rect.toRectangle()).toList()
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
                    shape.toRectangle()
                )
            )
            this.copy(shapes = shapes, rtree = rtree)
        }

        private fun Shape.toRectangle() = Rectangle(
            this.left,
            this.top,
            this.right,
            this.bottom
        )
    }
}
