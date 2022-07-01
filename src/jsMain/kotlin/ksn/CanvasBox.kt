package ksn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
actual inline fun CanvasBox(
    width: Dp,
    height: Dp,
    scale: Float,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .width(width * scale)
            .height(height * scale),
        content = content
    )
}
