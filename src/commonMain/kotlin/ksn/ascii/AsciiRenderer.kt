package ksn.ascii

import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

object AsciiRenderer {

    // depends on OS
    private const val APPLE_EMOJI_SCALE = 0.9f
    private const val APPLE_EMOJI_OFFSET_X = -1f
    private const val APPLE_EMOJI_OFFSET_Y = 17f

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
        val emojis = mutableListOf<AsciiChar.Emoji>()
        val textLineList = ascii.render {
            if (it is AsciiChar.Emoji) {
                emojis.add(it)
            }
            it.value
        }.map { asciiLine ->
            TextLine.make(asciiLine, Font(typeFace, fontHeight))
        }

        nativeCanvas.apply {
            translate(offsetX, offsetY)
            scale(scale, scale)
            textLineList.fold(14f) { sum, textLine ->
                drawTextLine(textLine, 0f, sum, paint)
                sum + fontHeight
            }
            emojis.forEach { emoji ->
                val emojiText = TextLine.make(
                    emoji.emoji.emoji,
                    Font(typeFace, fontHeight * APPLE_EMOJI_SCALE)
                )
                drawTextLine(
                    emojiText,
                    emoji.x * fontHeight / 2 + APPLE_EMOJI_OFFSET_X,
                    emoji.y * fontHeight + APPLE_EMOJI_OFFSET_Y,
                    paint
                )
            }
        }
    }
}
