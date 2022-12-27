package ksn.update

import elm.None
import elm.Pure
import elm.Sub
import elm.plus
import ksn.Constants
import ksn.model.DragType
import ksn.model.HandlePosition
import ksn.model.Point
import ksn.model.RTreeEntry
import ksn.model.SelectState.Moving
import ksn.model.SelectState.Resize
import ksn.model.ShapeMap
import ksn.model.Tool
import ksn.model.minus
import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import ksn.model.shape.TextBox
import ksn.model.shape.createAnchorHandle
import ksn.toDragStatus
import ksn.toKsnLine
import ksn.toKsnRect
import ksn.toRTreePoint
import ksn.toRTreeRectangle
import ksn.toSkiaRect
import ksn.ui.SkiaDragStatus
import ksn.ui.createResizeHandle
import ksn.ui.inside
import rtree.RTree
import rtree.Rectangle

data class DragStatus(
    val start: Point,
    val end: Point,
) {
    data class DragStart(val skiaDragStatus: SkiaDragStatus): Msg
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
            if (model.selectShapeIdSet.isEmpty()) {
                return model + None
            }
            if (model.selectShapeIdSet.size == 1) {
                val shape = model.shapes[model.selectShapeIdSet.first()] ?: return model + None
                val handlePosition = shape
                    .toSkiaRect()
                    .createResizeHandle(10f)
                    .firstOrNull { (rect, _) ->
                        rect.inside(msg.skiaDragStatus.start)
                    }?.second

                if (handlePosition != null) {
                    return model.copy(
                        tool = Tool.Select(Resize(handlePosition))
                    ) + None
                }
            }

            val dragStatus = msg.skiaDragStatus.toDragStatus()
            val shapeIdList = model.rtree.search(
                dragStatus.start.toRTreePoint()
            ).map { it.value }

            return if (shapeIdList.intersect(model.selectShapeIdSet).isNotEmpty()) {
                model.copy(
                    tool = Tool.Select(Moving)
                ) + None
            } else {
                model + None
            }
        }

        fun handleDrag(
            model: AppModel,
            msg: Drag
        ): Pure<AppModel> {
            return when {
                model.tool is Tool.Select && model.tool.state == Moving -> {
                    val point = msg.dragStatus.dragValue
                    model.copy(
                        dragType = DragType.DragMoving(point),
                    ) + None
                }
                model.tool is Tool.Select && model.tool.state is Resize -> {
                    val point = msg.dragStatus.dragValue
                    model.copy(
                        dragType = DragType.DragResize(point, model.tool.state.handlePosition)
                    ) + None
                }
                else -> model + None
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
                    val connect = findConnect(model, msg)
                    val lineId = model.maxId.getAndIncrement()
                    val line = msg.dragStatus.toKsnLine(connect)
                    val updatedModel = model.returnUpdateModel(lineId, line)
                    if (connect is Line.Connect.None) {
                        updatedModel
                    } else {
                        val connectIdList = connect.getIdList()
                        val updatedShapeMap = updateConnectShapeInList(updatedModel.shapes.clone(), connectIdList, lineId)
                        updatedModel.copy( shapes = updatedShapeMap)
                    } + None
                }
                is Tool.Text -> {
                    val id = model.maxId.getAndIncrement()
                    val rect = msg.dragStatus.toKsnRect()
                    val textBox = TextBox(rect, "TEXT")
                    model.returnUpdateModel(id, textBox).copy(
                        selectShapeIdSet = setOf(id)
                    ) + AppModel.ShowTextFieldCmd(rect, model.inputTextFieldHostState)
                }
                is Tool.Select -> when (model.tool.state) {
                    is Moving -> {
                        // TODO remove connect when move 'list'
                        val (shapes, rtree) = moveShapeAndRTree(model, msg.dragStatus.dragValue)
                        model.copy(
                            tool = Tool.Select(),
                            shapes = shapes,
                            rtree = rtree,
                            dragType = DragType.Zero,
                        ) + None
                    }
                    is Resize -> {
                        val (shapes, rtree) = if (model.dragType is DragType.DragResize) {
                            resizeShapeAndRTree(model, model.dragType)
                        } else {
                            model.shapes to model.rtree
                        }
                        model.copy(
                            tool = Tool.Select(),
                            shapes = shapes,
                            rtree = rtree,
                            dragType = DragType.Zero,
                        ) + None
                    }
                    else -> {
                        val rTreeRect = msg.dragStatus.toRTreeRectangle()
                        val result = model.rtree.search(rTreeRect).toList()
                        val selectedIdSet = result.map { it.value }.toSet()
                        model.copy(
                            tool = Tool.Select(),
                            selectShapeIdSet = selectedIdSet,
                            dragType = DragType.Zero,
                        ) + None
                    }
                }
                else -> model + None
            }
        }

        private fun updateConnectShapeInList(
            shapeMap: ShapeMap,
            connectIdList: List<Long>,
            lineId: Long
        ): ShapeMap {
            connectIdList.forEach { id ->
                shapeMap.update(id) { shape ->
                    if (shape is Rect) {
                        shape.copy(
                            connectLine = shape.connectLine + listOf(lineId)
                        )
                    } else {
                        null
                    }
                }
            }
            return shapeMap
        }

        private fun findConnect(model: AppModel, msg: DragEnd): Line.Connect {
            val startId = findConnectShapeId(model, msg.dragStatus.start)
            val endId = findConnectShapeId(model, msg.dragStatus.end)
            // TODO fix first and second
            return when {
                startId != null && endId != null -> Line.Connect.Both(
                    Line.Connect.Start(startId.first, startId.second),
                    Line.Connect.End(endId.first, endId.second),
                )
                startId != null -> Line.Connect.Start(startId.first, startId.second)
                endId != null -> Line.Connect.End(endId.first, endId.second)
                else -> Line.Connect.None
            }
        }

        private fun findConnectShapeId(model: AppModel, point: Point): Pair<Long, HandlePosition>? {
            val (connectShape, handlePosition) = model.rtree.search(
                point.toRTreePoint(),
                Constants.LINE_ANCHOR_DISTANCE
            ).asSequence().mapNotNull { (id, _) ->
                // TODO need rectangle.createAnchorHandle
                model.shapes[id]?.withId(id)
            }.firstNotNullOfOrNull { shape ->
                val (_, handlePosition) = shape.shape.createAnchorHandle().find { (p, _) ->
                    p == point
                } ?: return@firstNotNullOfOrNull null
                shape to handlePosition
            } ?: return null
            return connectShape.id to handlePosition
        }

        private fun AppModel.returnUpdateModel(id: Long, shape: Shape) = if (shape.isEmpty) {
            this
        } else {
            val newShapeMap = this.shapes.clone()
            newShapeMap[id] = shape
            val rtree = this.rtree.add(
                RTreeEntry(
                    id,
                    shape.toRTreeRectangle()
                )
            )
            this.copy(shapes = newShapeMap, rtree = rtree, dragType = DragType.Zero)
        }

        private fun moveShapeAndRTree(model: AppModel, dragValue: Point): Pair<ShapeMap, RTree<Long, Rectangle>> {
            val rtreeTranslate = mutableListOf<Translate>()

            val newShapeMap = model.shapes.clone()
            model.selectShapeIdSet.forEach { id ->
                newShapeMap.update(id) { shape ->
                    val newShape = shape.translate(dragValue)
                    rtreeTranslate.add(
                        Translate(
                            id,
                            shape.toRTreeRectangle(),
                            newShape.toRTreeRectangle()
                        )
                    )
                    newShape
                }
            }
            val translateIdList = rtreeTranslate.map { it.id }.toSet()
            newShapeMap.updateAllInstance<Line> { (id, line) ->
                val newLine = line.getConnectIdList().intersect(translateIdList).fold(line) { _: Line, shapeId: Long ->
                    line.connectTranslate(dragValue, shapeId)
                }
                if (newLine != line) {
                    rtreeTranslate.add(
                        Translate(
                            id,
                            line.toRTreeRectangle(),
                            newLine.toRTreeRectangle()
                        )
                    )
                }
                newLine
            }
            val rtree = model.rtree.move(rtreeTranslate)
            return newShapeMap to rtree
        }

        private fun resizeShapeAndRTree(model: AppModel, dragType: DragType.DragResize): Pair<ShapeMap, RTree<Long, Rectangle>> {
            val rtreeTranslate = mutableListOf<Translate>()
            val newShapeMap = model.shapes.clone()
            model.selectShapeIdSet.forEach { id ->
                newShapeMap.update(id) { shape ->
                    val newShape = shape.resize(dragType.point, dragType.handlePosition)
                    rtreeTranslate.add(
                        Translate(
                            id,
                            shape.toRTreeRectangle(),
                            newShape.toRTreeRectangle()
                        )
                    )
                    newShape
                }
            }
            val rtree = model.rtree.move(rtreeTranslate)
            return newShapeMap to rtree
        }

        private fun RTree<Long, Rectangle>.move(
            translateList: List<Translate>
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
