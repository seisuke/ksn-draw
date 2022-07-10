import androidx.compose.foundation.layout.Column
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.window.Window
import ksn.App
import ksn.InitElementAndLocalProvideThat
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        Window("ksn-draw") {
            InitElementAndLocalProvideThat {
                Column {
                    OutlinedTextField(
                        value = "abc",
                        onValueChange = {}
                    )
                    App()
                }
            }
        }
    }
}
