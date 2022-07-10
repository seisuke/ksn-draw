package ksn.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val checked by element.mapAsState { model -> model.tool::class == tool::class }
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
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onSurface
            }
        )

        val background by animateColorAsState(
            if (checked) {
                MaterialTheme.colors.background
            } else {
                MaterialTheme.colors.background
            }
        )

        Column (
            modifier = Modifier
                .width(64.dp)
                .background(
                    color = background,
                    shape = RoundedCornerShape(4.dp)
                ) ,

            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Icon(
                tool.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = tool.label,
                color = tint
            )
        }
    }
}
