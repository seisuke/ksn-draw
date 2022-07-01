import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ksn.App


fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        title = "ksn-draw",
        icon = painterResource("assets/icon.png"),
        state = windowState
    ) {
        MaterialTheme {
            App(
                /*requestWindowSize = { w, h ->
                    windowState.size = windowState.size.copy(width = w, height = h)
                }*/
            )
        }
    }
}

@Preview
@Composable
fun preview() {
    MaterialTheme {
        App( )
    }
}
