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
        val moving: Boolean = false
    ) : Tool("Select",  Icons.Outlined.Select)
    object Rect : Tool("Rect",  Icons.Outlined.Rect)
    object Text : Tool("Text",  Icons.Outlined.Text)
    object Line : Tool("Line",  Icons.Outlined.Line)
    object Export : Tool("Export",  Icons.Outlined.Export)
}
