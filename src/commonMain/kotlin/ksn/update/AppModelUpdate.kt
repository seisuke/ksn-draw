package ksn.update

import elm.None
import elm.Sub
import elm.Update
import elm.plus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ksn.loadTypeface
import ksn.update.DragStatus.Companion.handleDrag
import ksn.update.DragStatus.Companion.handleDragEnd
import ksn.update.DragStatus.Companion.handleDragStart
import ksn.update.KeyMsg.Companion.handleKeyMsg

class AppModelUpdate : Update<AppModel, Msg, Cmd> {
    override fun update(msg: Msg, model: AppModel): Sub<AppModel, Cmd> = when (msg) {
        is KeyMsg -> handleKeyMsg(model, msg.key)
        is AppModel.CurrentTool -> model.copy(tool = msg.tool) + None
        AppModel.StartLoadFont -> model + AppModel.LoadFont
        is AppModel.LoadFontResult -> model.copy(typeface = msg.typeface) + None
        is AppModel.ExportClipBoard -> {
            model.exportClipBoard(msg.clipBoard)
            model + AppModel.ShowSnackBarCmd("export ascii", model.snackbarHostState)
        }
        is AppModel.ShowSnackBar -> model + AppModel.ShowSnackBarCmd(msg.message, model.snackbarHostState)
        is DragStatus.DragStart -> handleDragStart(model, msg)
        is DragStatus.Drag -> handleDrag(model, msg)
        is DragStatus.DragEnd -> handleDragEnd(model, msg)
        //else -> model + None //all msg are handled but sometimes error says "add necessary 'else' branch" in build
    }


    override fun call(cmd: Cmd): Flow<Msg> = flow {
        when (cmd) {
            AppModel.LoadFont -> {
                val typeface = loadTypeface()
                emit(
                    AppModel.LoadFontResult(typeface)
                )
            }
            is AppModel.ShowSnackBarCmd -> {
                cmd.snackbarHostState.showSnackbar(cmd.message)
            }
        }
    }

    override fun onUnhandledError(cmd: Cmd, t: Throwable): Msg {
        TODO("Not yet implemented")
    }
}
