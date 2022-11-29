package ksn.update

import elm.None
import elm.Pure
import elm.Sub
import elm.plus
import ksn.model.Point
import ksn.model.RTreeEntry
import ksn.model.Tool
import ksn.model.minus
import ksn.model.shape.Shape
import ksn.model.shape.TextBox
import ksn.toKsnLine
import ksn.toKsnRect
import ksn.toRTreeRectangle
import rtree.RTree
import rtree.Rectangle
import rtree.Point as RTreePoint

data class DragStatus(
    val start: Point,
    val end: Point,
) {
    data class DragStart(val dragStatus: DragStatus): Msg
    data class Drag(val dragStatus: DragStatus): Msg
    data class DragEnd(val dragStatus: DragStatus): Msg

    val isNotEmpty = start.x != end.x || start.y != end.y

    val dragValue = end - start

    private data class Translate(
        val id: Long,
        val prevRectangle: Rectangle,
        val newRectangle: Rectangle,
    )

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

            // TODO fix intersect to improve performance
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
                val drag = msg.dragStatus.dragValue
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
        ): Sub<AppModel, Cmd> {
            return when (model.tool) {
                is Tool.Rect -> {
                    val id = model.maxId.getAndIncrement()
                    val rect = msg.dragStatus.toKsnRect()
                    model.returnUpdateModel(id, rect) + None
                }
                is Tool.Line -> {
                    val id = model.maxId.getAndIncrement()
                    val line = msg.dragStatus.toKsnLine()
                    model.returnUpdateModel(id, line) + None
                }
                is Tool.Text -> {
                    val id = model.maxId.getAndIncrement()
                    val rect = msg.dragStatus.toKsnRect()
                    val textBox = TextBox(rect, "TEXT")
                    model.returnUpdateModel(id, textBox).copy(
                        selectShapeIdList = listOf(id)
                    ) + AppModel.ShowTextFieldCmd(rect, model.inputTextFieldHostState)
                }
                is Tool.Select -> if (model.tool.moving) {
                    val (shapes, rtree) = moveShapeAndRTree(model, msg.dragStatus.dragValue)
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

        private fun AppModel.returnUpdateModel(id: Long, shape: Shape) = if (shape.isEmpty) {
            this
        } else {
            val shapes = this.addShape(
                ShapeWithID(id, shape)
            )
            val rtree = this.rtree.add(
                RTreeEntry(
                    id,
                    shape.toRTreeRectangle()
                )
            )
            this.copy(shapes = shapes, rtree = rtree, drag = Point.Zero)
        }

        private fun moveShapeAndRTree(model: AppModel, dragValue: Point): Pair<List<ShapeWithID>, RTree<Long, Rectangle>> {
            val rtreeTranslate = mutableListOf<Translate>()
            val shapes = model.shapes.map { (id, shape) ->
                if (model.selectShapeIdList.contains(id)) {
                    val newShape = shape.translate(dragValue)
                    rtreeTranslate.add(
                        Translate(
                            id,
                            shape.toRTreeRectangle(),
                            newShape.toRTreeRectangle()
                        )
                    )
                    ShapeWithID(id, newShape)
                } else {
                    ShapeWithID(id, shape)
                }
            }
            val rtree = model.rtree.move(rtreeTranslate)
            return shapes to rtree
        }

        private fun RTree<Long, Rectangle>.move(
            translateList: MutableList<Translate>
        ): RTree<Long, Rectangle> = translateList.fold(this) { rtree, translate ->
            rtree.delete(
                RTreeEntry(
                    translate.id,
                    translate.prevRectangle
                )
            ).add(
                RTreeEntry(
                        translate.id,
                    translate.newRectangle
                )
            )
        }
    }
}
