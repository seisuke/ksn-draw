package ksn

import ksn.ui.CanvasView
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elm.Element
import kotlinx.atomicfu.atomic
import ksn.model.Tool
import ksn.ui.toolButton
import ksn.update.AppModel
import ksn.update.AppModelUpdate
import ksn.update.Msg
import org.jetbrains.skia.Typeface

val ModelElement = compositionLocalOf<Element<AppModel, Msg>> {
    error("No current Element")
}

@Composable
fun App(
    //requestWindowSize: ((width: Dp, height: Dp) -> Unit)? = null
) = MainLayout {

    val appModelElement = Element.create(
        AppModel(
            title = "hello",
            tool = Tool.Rect,
            maxId = atomic(1L),
        ),
        AppModelUpdate(),
        rememberCoroutineScope()
    )

    CompositionLocalProvider(
        ModelElement provides appModelElement
    ) {
        mainView()
    }
}

@Composable
private fun MainLayout(block:@Composable ColumnScope.() -> Unit) {
    Column { block() }
}

@Composable
fun mainView() {
    Column {
        val element = ModelElement.current
        val typeface by element.mapAsState(AppModel::typeface)
        Row {
            toolButton(Tool.Select)
            toolButton(Tool.Rect)
            toolButton(Tool.Text)
            toolButton(Tool.Line)
            toolButton(Tool.Export)
        }

        Spacer(modifier = Modifier.height(20.dp))

        CanvasView()
        if (typeface == null) {
            element.accept(AppModel.StartLoadFont)
        }
    }
}

expect suspend fun loadTypeface(): Typeface

@Composable
expect inline fun CanvasBox(
    width: Dp,
    height: Dp,
    scale: Float,
    content: @Composable BoxScope.() -> Unit
)
