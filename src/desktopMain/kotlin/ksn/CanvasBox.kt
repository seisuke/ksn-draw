package ksn

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .width(width)
            .height(height)
    ) {
        val stateVertical = rememberScrollState()
        val stateHorizontal = rememberScrollState()

        Box(
            modifier = Modifier
                .verticalScroll(stateVertical)
                .horizontalScroll(stateHorizontal)
        ) {
            content()
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(stateVertical)
        )
        HorizontalScrollbar(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            adapter = rememberScrollbarAdapter(stateHorizontal)
        )
    }
}
