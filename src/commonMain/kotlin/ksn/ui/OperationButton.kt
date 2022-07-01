package ksn.ui

import ksn.ModelElement
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import ksn.update.AppModel
import ksn.model.Tool

@Composable
fun operationButton(
    tool: Tool
) {
    val element = ModelElement.current
    val checked by element.mapAsState { model -> model.tool == tool }

    IconToggleButton(
        checked = checked,
        onCheckedChange = {
            if (it) {
                element.accept(
                    AppModel.CurrentTool(tool)
                )
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
