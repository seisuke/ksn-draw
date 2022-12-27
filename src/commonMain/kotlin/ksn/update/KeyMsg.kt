package ksn.update

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import elm.None
import elm.Sub
import elm.plus
import ksn.model.RTreeEntry
import ksn.toRTreeRectangle

class KeyMsg(val key: Key) : Msg {

    companion object {

        @OptIn(ExperimentalComposeUiApi::class)
        fun handleKeyMsg(model: AppModel, key: Key): Sub<AppModel, Cmd> {
            return if (key == Key.Backspace && model.selectShapeIdSet.isNotEmpty()) {
                val entries = model.selectedShapes().map { (id, shape) ->
                    RTreeEntry(id, shape.toRTreeRectangle())
                }
                val rtree = model.rtree.delete(entries)
                val newShapeMap = model.shapes.clone()
                model.selectShapeIdSet.map { id ->
                    newShapeMap.remove(id)
                }
                model.copy(
                    shapes = newShapeMap,
                    rtree = rtree,
                    selectShapeIdSet = emptySet(),
                ) + None
            } else {
                model + None
            }
        }
    }
}
