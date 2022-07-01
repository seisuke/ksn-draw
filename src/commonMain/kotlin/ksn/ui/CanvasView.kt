package ksn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ksn.CanvasBox
import ksn.ascii.Ascii
import ksn.ascii.AsciiChar
import ksn.ascii.Matrix

@Composable
fun CanvasView() {

    val columnNumber = 32
    val width = (columnNumber * 10).dp
    val ascii = Ascii(
        Matrix.init(
            columnNumber,
            columnNumber,
            AsciiChar.Char(Ascii.SPACE)
        )
    )

    val scale = 1f

    CanvasBox(
        width = width,
        height = width * 2,
        scale = scale
    ) {
        UiLayer(width, scale)
        GridLayer(width, scale)
        AsciiLayer(width, scale, ascii)
    }
}

