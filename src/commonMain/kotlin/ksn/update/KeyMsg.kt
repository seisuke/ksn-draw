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
            return if (key == Key.Backspace && model.selectShapeIdList.isNotEmpty()) {
                val entries = model.selectedShapes().map { shape ->
                    RTreeEntry(shape.id, shape.toRTreeRectangle())
                }
                val rtree = model.rtree.delete(entries)
                val shapes = model.shapes.filter { shape ->
                    !model.selectShapeIdList.contains(shape.id)
                }
                model.copy(
                    shapes = shapes,
                    rtree = rtree,
                    selectShapeIdList = emptyList(),
                ) + None
            } else {
                model + None
            }
        }
    }
}
