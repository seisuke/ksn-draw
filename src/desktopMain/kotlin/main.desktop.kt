import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ksn.App
import ksn.ui.icons.Select


fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "ksn-draw",
        icon = painterResource("assets/icon.png"),
        state = windowState
    ) {
        App(
                /*requestWindowSize = { w, h ->
                    windowState.size = windowState.size.copy(width = w, height = h)
                }*/
        )
    }
}

@Preview
@Composable
fun preview() {
    MaterialTheme {
        val icon = Icons.Outlined.Select
        Icon(
            painter = rememberVectorPainter(image = icon),
            contentDescription = null,
            modifier = Modifier.padding(4.dp),
        )
    }
}
