import androidx.compose.foundation.layout.Column
import androidx.compose.ui.window.Window
import ksn.App
import ksn.InitElementAndLocalProvideThat
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.skiko.wasm.onWasmReady


fun main() {

    renderComposable(rootElementId = "root") {
        Div {
            Text("abc")
        }
    }

    onWasmReady {
        Window("ksn-draw") {
            InitElementAndLocalProvideThat {
                Column {
                    App()
                }
            }
        }
    }
}
