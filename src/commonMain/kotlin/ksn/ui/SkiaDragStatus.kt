package ksn.ui

import androidx.compose.ui.geometry.Offset

data class SkiaDragStatus(
    val start: Offset,
    val end: Offset,
) {

    companion object {
        val Zero = SkiaDragStatus(
            Offset.Zero,
            Offset.Zero
        )
    }
}
