package ksn.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Outlined.Text : ImageVector
    get() {
        if (_menu != null) {
            return _menu!!
        }
        _menu = materialIcon(name = "Rounded.Menu") {
            materialPath {
                moveTo(11.0f, 4.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(10.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(18.0f)
                verticalLineTo(10.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(6.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(4.0f)
                moveTo(8.0f, 10.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(13.59f)
                lineTo(11.59f, 14.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(16.0f)
                horizontalLineTo(11.0f)
                verticalLineTo(18.0f)
                horizontalLineTo(10.0f)
                verticalLineTo(20.0f)
                horizontalLineTo(13.0f)
                verticalLineTo(16.0f)
                horizontalLineTo(20.0f)
                verticalLineTo(14.0f)
                horizontalLineTo(14.21f)
                lineTo(16.0f, 12.21f)
                verticalLineTo(10.0f)
                close()
            }
        }
        return _menu!!
    }

private var _menu: ImageVector? = null
