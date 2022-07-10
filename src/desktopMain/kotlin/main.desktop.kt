import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ksn.App
import ksn.InitElementAndLocalProvideThat
import ksn.ModelElement
import ksn.ui.icons.Select
import ksn.update.KeyMsg

fun main() = application {
    val windowState = rememberWindowState()

    InitElementAndLocalProvideThat {
        val element = ModelElement.current
        Window(
            onCloseRequest = ::exitApplication,
            resizable = true,
            title = "ksn-draw",
            icon = painterResource("assets/icon.png"),
            state = windowState,
            onKeyEvent = { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    element.accept(
                        KeyMsg(keyEvent.key)
                    )
                }
                false
            }
        ) {
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
        val icon = Icons.Outlined.Select
        Icon(
            painter = rememberVectorPainter(image = icon),
            contentDescription = null,
            modifier = Modifier.padding(4.dp),
        )
    }
}
