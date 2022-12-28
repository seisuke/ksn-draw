package ksn.model.shape

import ksn.Constants.Companion.GRID_WIDTH
import ksn.model.DragType
import ksn.model.HandlePosition
import ksn.model.HandlePosition.CENTER_BOTTOM
import ksn.model.HandlePosition.CENTER_TOP
import ksn.model.HandlePosition.LEFT_BOTTOM
import ksn.model.HandlePosition.LEFT_MIDDLE
import ksn.model.HandlePosition.LEFT_TOP
import ksn.model.HandlePosition.RIGHT_BOTTOM
import ksn.model.HandlePosition.RIGHT_MIDDLE
import ksn.model.HandlePosition.RIGHT_TOP
import ksn.model.Point
import ksn.model.plus
import kotlin.math.max
import kotlin.math.min

sealed interface Shape {
    val left: Int
    val top: Int
    val right: Int
    val bottom: Int
    val connectLine: List<Long>
    val width: Int
        get() = right - left + 1
    val height: Int
        get() = bottom - top + 1
    val verticalCenter: Int
        get() = (top + bottom) / 2
    val horizontalCenter: Int
        get() = (left + right) / 2
    val isEmpty: Boolean

    fun Int.toSkiaFloat(): Float = (this * GRID_WIDTH).toFloat()

    fun translate(point: Point): Shape

    fun resize(point: Point, handlePosition: HandlePosition): Shape

    //FIXME subclass type is removed
    fun drag(dragType: DragType): Shape = when (dragType) {
        is DragType.DragMoving -> translate(dragType.point)
        is DragType.DragResize -> resize(
            dragType.point,
            dragType.handlePosition
        )
        else -> this
    }
}

data class Rect(
    override val left: Int,
    override val top: Int,
    override val right: Int,
    override val bottom: Int,
    override val connectLine: List<Long> = emptyList()
): Shape {
    override val isEmpty: Boolean
        get() = width <= 1 || height <= 1

    override fun translate(point: Point): Shape = Rect(
        left + point.x,
        top + point.y,
        right + point.x,
        bottom + point.y,
    )

    // TODO validate point
    override fun resize(point: Point, handlePosition: HandlePosition): Shape = when (handlePosition) {
        LEFT_TOP -> Rect(
            left = left + point.x,
            top = top + point.y,
            right = right,
            bottom = bottom,
        )
        LEFT_MIDDLE -> Rect(
            left = left + point.x,
            top = top,
            right = right,
            bottom = bottom,
        )
        LEFT_BOTTOM -> Rect(
            left = left + point.x,
            top = top,
            right = right,
            bottom = bottom + point.y,
        )
        CENTER_BOTTOM -> Rect(
            left = left,
            top = top,
            right = right,
            bottom = bottom + point.y,
        )
        RIGHT_BOTTOM -> Rect(
            left = left,
            top = top,
            right = right + point.x,
            bottom = bottom + point.y,
        )
        RIGHT_MIDDLE -> Rect(
            left = left,
            top = top,
            right = right + point.x,
            bottom = bottom,
        )
        RIGHT_TOP -> Rect(
            left = left,
            top = top + point.y,
            right = right + point.x,
            bottom = bottom,
        )
        CENTER_TOP -> Rect(
            left = left,
            top = top + point.y,
            right = right,
            bottom = bottom,
        )
    }
}

data class Line(
    val start: Point,
    val end: Point,
    val connect: Connect = Connect.None
): Shape {
    // TODO add another pattern when line connect with shape
    override val left: Int = min(start.x, end.x)
    override val top: Int = min(start.y, end.y)
    override val right: Int = max(start.x, end.x)
    override val bottom: Int = max(start.y, end.y)
    override val connectLine: List<Long>
        get() = emptyList()

    override val isEmpty: Boolean
        get() = width <= 1 && height <= 1

    override fun translate(point: Point) = Line(
        start + point,
        end + point,
        connect = connect
    )

    fun getConnectIdList(): List<Long> = connect.getIdList()

    fun connectTranslate(point: Point, shapeId: Long): Line {
        return when (val connect = connect) {
            is Connect.Start -> {
                if (connect.id == shapeId) {
                    this.copy(start = this.start + point)
                } else {
                    this
                }
            }
            is Connect.End -> {
                if (connect.id == shapeId) {
                    this.copy(end = this.end + point)
                } else {
                    this
                }
            }
            is Connect.Both -> {
                if (connect.start.id == shapeId) {
                    this.copy(start = this.start + point)
                } else if (connect.end.id == shapeId) {
                    this.copy(end = this.end + point)
                } else {
                    this
                }
            }
            else -> this
        }
    }

    fun connectResize(shapeId: Long, shape: Shape): Line {
        val anchorHandleList = shape.createAnchorHandle()
        val connectList = when (val connect = connect) {
            is Connect.Start -> listOf(connect)
            is Connect.End -> listOf(connect)
            is Connect.Both -> listOf(connect.start, connect.end)
            is Connect.None -> emptyList()
        }
        return connectList.fold(this) { acc, connect ->
            when (connect) {
                is Connect.Start -> {
                    val connectPoint = findMoveConnectPoint(
                        connect.id,
                        shapeId,
                        connect.handlePosition,
                        anchorHandleList
                    ) ?: return@fold acc
                    acc.copy(start = connectPoint)
                }
                is Connect.End -> {
                    val connectPoint = findMoveConnectPoint(
                        connect.id,
                        shapeId,
                        connect.handlePosition,
                        anchorHandleList
                    ) ?: return@fold acc
                    acc.copy(end = connectPoint)
                }
                else -> acc
            }
        }
    }

    override fun resize(point: Point, handlePosition: HandlePosition): Shape = this

    private fun findMoveConnectPoint(
        connectId: Long,
        shapeId: Long,
        connectHandlePosition: HandlePosition,
        anchorHandleList: List<Pair<Point, HandlePosition>>
    ): Point? = if (connectId == shapeId) {
        val anchorHandle = anchorHandleList.firstOrNull { (_, handlePosition) ->
            connectHandlePosition == handlePosition
        }
        anchorHandle?.first
    } else {
        null
    }

    sealed class Connect {
        object None : Connect()
        data class Start(
            val id: Long,
            val handlePosition: HandlePosition
        ) : Connect()
        data class End(
            val id: Long,
            val handlePosition: HandlePosition
        ) : Connect()
        data class Both(
            val start: Start,
            val end: End
        ) : Connect()

        fun getIdList(): List<Long> {
            return when (this) {
                is Start -> listOf(this.id)
                is End -> listOf(this.id)
                is Both -> listOf(this.start.id, this.end.id)
                None -> emptyList()
            }
        }
    }
}

data class TextBox(
    val rect: Rect,
    val text: String,
): Shape by rect {

    override fun translate(point: Point): Shape = TextBox(
        rect = rect.translate(point) as Rect,
        text = text,
    )

    override fun resize(point: Point, handlePosition: HandlePosition): Shape = TextBox(
        rect = rect.resize(point, handlePosition) as Rect,
        text = text,
    )
}

fun Shape.createAnchorHandle(): List<Pair<Point, HandlePosition>> = listOf(
    Point(left - 1, verticalCenter) to LEFT_MIDDLE,
    Point(right + 1, verticalCenter) to RIGHT_MIDDLE,
    Point(horizontalCenter, top - 1) to CENTER_TOP,
    Point(horizontalCenter, bottom + 1) to CENTER_BOTTOM,
)
