import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import ksn.App
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        Window("ksn-draw") {
            MaterialTheme {
                App()
            }
        }
    }
}
