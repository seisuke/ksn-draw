package ksn.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
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
                val msg = if (tool is Tool.Export) {
                    AppModel.ExportClipBoard(
                        clipboardManager
                    )
                } else {
                    AppModel.CurrentTool(tool)
                }
                element.accept(msg)
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

        Column (
            modifier = Modifier.wrapContentWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Icon(
                tool.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )

            Text(text = tool.label)
        }
    }
}
