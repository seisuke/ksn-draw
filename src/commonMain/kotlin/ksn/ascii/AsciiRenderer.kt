package ksn.ascii

import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

object AsciiRenderer {

    fun drawAscii(
        nativeCanvas: NativeCanvas,
        paint: Paint,
        typeFace: Typeface,
        ascii: Ascii,
        scale: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        fontHeight: Float = 20.dp.value,
    ) {
        val textLineList = ascii.render {
            it.value
        }.map { asciiLine ->
            TextLine.make(asciiLine, Font(typeFace, fontHeight))
        }

        nativeCanvas.apply {
            //clear(Color.TRANSPARENT)
            translate(offsetX, offsetY)
            scale(scale, scale)
            textLineList.fold(14f) { sum, textLine ->
                drawTextLine(textLine, 0f, sum, paint)
                sum + fontHeight
            }
        }
    }
}
