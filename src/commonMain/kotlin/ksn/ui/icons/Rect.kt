package ksn.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Outlined.Rect : ImageVector
    get() {
        if (_menu != null) {
            return _menu!!
        }
        _menu = materialIcon(name = "Rounded.Menu") {
            materialPath {
                moveTo(4.0f, 6.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(4.0f)
                moveTo(18.0f, 17.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(18.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
        return _menu!!
    }

private var _menu: ImageVector? = null
