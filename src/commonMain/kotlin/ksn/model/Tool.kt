package ksn.model

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import ksn.ui.icons.Export
import ksn.ui.icons.Line
import ksn.ui.icons.Rect
import ksn.ui.icons.Select
import ksn.ui.icons.Text

sealed class Tool(val label: String, val icon: ImageVector) {
    data class Select(
        val state: SelectState = SelectState.None
    ) : Tool("Select",  Icons.Outlined.Select)
    object Rect : Tool("Rect",  Icons.Outlined.Rect)
    object Text : Tool("Text",  Icons.Outlined.Text)
    object Line : Tool("Line",  Icons.Outlined.Line)
    object Export : Tool("Export",  Icons.Outlined.Export)
}

sealed class SelectState {
    object None : SelectState()
    object Moving : SelectState()
    data class Resize(
        val handlePosition: HandlePosition,
    ) : SelectState()
}

enum class HandlePosition {
    LEFT_TOP,    CENTER_TOP,    RIGHT_TOP,
    LEFT_MIDDLE,                RIGHT_MIDDLE,
    LEFT_BOTTOM, CENTER_BOTTOM, RIGHT_BOTTOM;
}

sealed class DragType(open val point: Point) {
    data class DragMoving(
        override val point: Point,
    ) : DragType(point)
    data class DragResize(
        override val point: Point,
        val handlePosition: HandlePosition,
    ) : DragType(point)
    object Zero : DragType(Point.Zero)
}
