package ksn

import org.jetbrains.skia.Typeface
import org.jetbrains.skia.makeFromFile

actual suspend fun loadTypeface() = Typeface.makeFromFile(
    "src/commonMain/resources/assets/SFMonoSquare-Regular.otf"
)
