package ksn.update

import androidx.compose.material.SnackbarHostState
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.atomicfu.AtomicLong
import ksn.ascii.Ascii
import ksn.ascii.AsciiChar
import ksn.ascii.Matrix
import ksn.model.Point
import ksn.model.Tool
import ksn.model.shape.Shape
import org.jetbrains.skia.Typeface
import rtree.RTree
import rtree.Rectangle

data class AppModel(
    val title: String,
    val tool: Tool,
    val maxId: AtomicLong,
    val shapes: List<Shape> = emptyList(),
    val selectShapeIdList: List<Long> = emptyList(),
    val rtree: RTree<Long, Rectangle> = RTree.create(
        emptyList()
    ),
    val drag: Point = Point.Zero,
    val typeface: Typeface? = null,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    data class CurrentTool(val tool: Tool): Msg
    object StartLoadFont: Msg
    data class LoadFontResult(val typeface: Typeface): Msg
    data class ExportClipBoard(val clipBoard: ClipboardManager): Msg
    data class ShowSnackBar(val message: String): Msg

    object LoadFont: Cmd
    data class ShowSnackBarCmd(val message: String, val snackbarHostState: SnackbarHostState): Cmd

    /**
     * Mutable method. Returns list of [AppModel.shapes] plus [shape]
     */
    fun addShape(shape: Shape): List<Shape> {
        return shapes + shape
    }

    fun selectedShapes(): List<Shape> {
        if (selectShapeIdList.isEmpty()) {
            return emptyList()
        }

        return shapes.filter { shape ->
            selectShapeIdList.contains(shape.id)
        }
    }

    fun exportClipBoard(clipBoard: ClipboardManager) {
        val maxRectangle = this.rtree.mbr() ?: return
        // TODO ascii size is strange when mbr size overhead grid
        val ascii = Ascii(
            Matrix.init(
                maxRectangle.x2 + 1,
                maxRectangle.y2 + 1,
                AsciiChar.Char(Ascii.SPACE)
            )
        )
        ascii.mergeToMatrix(this.shapes)
        val output = ascii.render(AsciiChar::value).joinToString(separator = "\n")
        clipBoard.setText(
            AnnotatedString(output)
        )
    }
}
