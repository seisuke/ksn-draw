package ksn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import elm.Element
import kotlinx.atomicfu.atomic
import ksn.model.Tool
import ksn.ui.CanvasView
import ksn.ui.toolButton
import ksn.update.AppModel
import ksn.update.AppModelUpdate
import ksn.update.Msg
import org.jetbrains.skia.Typeface

val ModelElement = compositionLocalOf<Element<AppModel, Msg>> {
    error("No current Element")
}

@Composable
fun InitElementAndLocalProvideThat(
    content: @Composable () -> Unit
) {
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
        content()
    }
}

@Composable
fun App(
    //requestWindowSize: ((width: Dp, height: Dp) -> Unit)? = null
) {
    MaterialTheme(
        colors = lightColors()
    ) {
        mainView()
    }
}

@Composable
fun mainView() {

    val element = ModelElement.current
    val typeface by element.mapAsState(AppModel::typeface)
    val snackbarHostState by element.mapAsState(AppModel::snackbarHostState)
    Box (
        modifier = Modifier.fillMaxSize()
    ){
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) { snackbarData ->
            Card(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = snackbarData.message)
                }
            }
        }

        Column {
            Row (
                modifier = Modifier.padding(16.dp)
            ){
                toolButton(Tool.Select())
                Spacer(Modifier.width(16.dp))
                toolButton(Tool.Rect)
                Spacer(Modifier.width(16.dp))
                toolButton(Tool.Text)
                Spacer(Modifier.width(16.dp))
                toolButton(Tool.Line)
                Spacer(Modifier.width(16.dp))
                toolButton(Tool.Export)
            }

            Spacer(modifier = Modifier.height(20.dp))

            CanvasView()
            if (typeface == null) {
                element.accept(AppModel.StartLoadFont)
            }
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
