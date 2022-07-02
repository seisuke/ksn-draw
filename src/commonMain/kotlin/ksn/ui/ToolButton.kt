package ksn.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import ksn.ModelElement
import ksn.model.Tool
import ksn.update.AppModel

@Composable
fun toolButton(
    tool: Tool
) {
    val element = ModelElement.current
    val checked by element.mapAsState { model -> model.tool == tool }
    val clipboardManager = LocalClipboardManager.current

    IconToggleButton(
        checked = checked,
        onCheckedChange = {
            if (it) {
                element.accept(
                    AppModel.CurrentTool(tool)
                )
                if (tool is Tool.Export) {
                    element.accept(
                        AppModel.ExportClipBoard(
                            clipboardManager
                        )
                    )
                }
            }
        }
    ) {
        val tint by animateColorAsState(
            if (checked) {
                Color(0xFFEC407A)
            } else {
                Color(0xFFB0BEC5)
            }
        )
        
        Icon(
            Icons.Filled.Favorite,
            contentDescription = null,
            tint = tint
        )

        Text(text = tool.label)
    }
}
