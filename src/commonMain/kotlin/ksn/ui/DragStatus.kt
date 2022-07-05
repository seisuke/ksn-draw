package ksn.ui

import androidx.compose.ui.geometry.Offset

data class DragStatus(
    val start: Offset,
    val end: Offset,
) {

    companion object {
        val Zero = DragStatus(
            Offset.Zero,
            Offset.Zero
        )
    }
}
