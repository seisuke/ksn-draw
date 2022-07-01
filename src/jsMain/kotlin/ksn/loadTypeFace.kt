package ksn

import org.jetbrains.skia.Data
import org.jetbrains.skia.Typeface
import org.jetbrains.skiko.loadBytesFromPath

actual suspend fun loadTypeface(): Typeface {
    val fontByteArray = loadBytesFromPath("assets/SFMonoSquare-Regular.otf")
    return Typeface.makeFromData(
        Data.makeFromBytes(fontByteArray)
    )
}

