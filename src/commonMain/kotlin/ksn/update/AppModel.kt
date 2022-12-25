package ksn.update

import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import elm.ModelOperator
import kotlinx.atomicfu.AtomicLong
import ksn.ascii.Ascii
import ksn.ascii.AsciiChar
import ksn.ascii.Matrix
import ksn.model.DragType
import ksn.model.Tool
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import org.jetbrains.skia.Typeface
import rtree.RTree
import rtree.Rectangle

data class AppModel(
    val title: String,
    val tool: Tool,
    val maxId: AtomicLong,
    val shapes: List<ShapeWithID> = emptyList(), // TODO should be TreeMap
    val selectShapeIdSet: Set<Long> = emptySet(),
    val rtree: RTree<Long, Rectangle> = RTree.create(
        emptyList()
    ),
    val dragType: DragType = DragType.Zero,
    val typeface: Typeface? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
    val inputTextFieldHostState: SnackbarHostState = SnackbarHostState(),
): ModelOperator {
    data class CurrentTool(val tool: Tool): Msg
    object StartLoadFont: Msg
    data class LoadFontResult(val typeface: Typeface): Msg
    data class ExportClipBoard(val clipBoard: ClipboardManager): Msg
    data class ShowSnackBar(val message: String): Msg
    data class TextBoxUpdate(val text: String): Msg

    object LoadFont: Cmd
    data class ShowSnackBarCmd(val message: String, val snackbarHostState: SnackbarHostState): Cmd
    data class ShowTextFieldCmd(val rect: Rect, val inputTextFieldHostState: SnackbarHostState): Cmd

    /**
     * Mutable method. Returns list of [AppModel.shapes] plus [shape]
     */
    fun addShape(shapeWithID: ShapeWithID): List<ShapeWithID> {
        return shapes + shapeWithID
    }

    fun selectedShapes(): List<ShapeWithID> {
        if (selectShapeIdSet.isEmpty()) {
            return emptyList()
        }

        return shapes.filter { shape ->
            selectShapeIdSet.contains(shape.id)
        }
    }

    fun exportClipBoard(clipBoard: ClipboardManager) {
        val maxRectangle = this.rtree.mbr() ?: return
        // TODO ascii size is strange when mbr size overhead grid
        val ascii = Ascii(
            Matrix.init(
                maxRectangle.x2 + 1,
                maxRectangle.y2 + 1,
                AsciiChar.Space
            )
        )
        ascii.mergeToMatrix(this.shapes.map(ShapeWithID::shape))
        val output = ascii.render {
            when(it) {
                is AsciiChar.Emoji -> { it.emoji.emoji }
                else -> it.value
            }
        }.joinToString(separator = "\n")
        clipBoard.setText(
            AnnotatedString(output)
        )
    }
}

//FIXME good name
data class ShapeWithID(
    val id: Long,
    val shape: Shape,
)

fun Shape.withId(id: Long) = ShapeWithID(id, this)
