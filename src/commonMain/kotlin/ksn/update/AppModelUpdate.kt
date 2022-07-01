package ksn.update

import elm.None
import elm.Sub
import elm.Update
import elm.plus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ksn.update.DragStatus.Companion.handleDragEnd
import ksn.loadTypeface

class AppModelUpdate : Update<AppModel, Msg, Cmd> {
    override fun update(msg: Msg, model: AppModel): Sub<AppModel, Cmd> = when (msg) {
        is AppModel.CurrentTool -> model.copy(tool = msg.tool) + None
        AppModel.StartLoadFont -> model + AppModel.LoadFont
        is AppModel.LoadFontResult -> model.copy(typeface = msg.typeface) + None
        is DragStatus.DragEnd -> handleDragEnd(model, msg)
        //else -> model + None //sometimes build output say "add necessary 'else' branch" why?
    }

    override fun call(cmd: Cmd): Flow<Msg> = flow {
        when (cmd) {
            AppModel.LoadFont -> {
                val typeface = loadTypeface()
                emit(
                    AppModel.LoadFontResult(typeface)
                )
            }
        }
    }

    override fun onUnhandledError(cmd: Cmd, t: Throwable): Msg {
        TODO("Not yet implemented")
    }
}
