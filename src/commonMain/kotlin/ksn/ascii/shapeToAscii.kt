package ksn.ascii

import io.github.seisuke.kemoji.EmojiParser
import io.github.seisuke.kemoji.TextOrSpan
import ksn.model.Point
import ksn.model.minus
import ksn.model.shape.Line
import ksn.model.shape.Rect
import ksn.model.shape.Shape
import ksn.model.shape.TextBox
import kotlin.math.ceil

fun Shape.toAsciiMatrix(): Matrix<AsciiChar> = when (this) {
    is Rect -> toAsciiMatrix(this)
    is Line -> toAsciiMatrix(this)
    is TextBox -> toAsciiMatrix(this)
}

private fun toAsciiMatrix(rect: Rect): Matrix<AsciiChar> {
    val matrix = Matrix.init<AsciiChar>(rect.width, rect.height, AsciiChar.Transparent)
    rect.toPointList()
        .toBoundingPointsList()
        .filterIsInstance<Edge>()
        .map(::toBounding)
        .run {
            toMutableList() + first()
        }
        .complementStroke()
        .forEach { (point, boundingType) ->
            matrix.set(point.x, point.y, AsciiChar.Bounding(boundingType))
        }
    return matrix
}

private fun toAsciiMatrix(textBox: TextBox): Matrix<AsciiChar> {
    val matrix = textBox.rect.toAsciiMatrix()
    var offset = 0
    var x = 1
    var y = 1

    EmojiParser.parseToSpanList(textBox.text) {
        it.emoji
    }.forEach {
        when(it) {
            is TextOrSpan.Span -> {
                val emoji = AsciiChar.Emoji(it.box, x + offset, y)
                matrix.set(x + offset, y, emoji)
                offset++
                matrix.set( x + offset, y, AsciiChar.FullWidthSpace)
                x++
            }
            is TextOrSpan.Text -> it.text.forEach { char ->
                if (char == '\n') {
                    offset = 0
                    x = 1
                    y++
                } else {
                    matrix.set(x + offset, y, AsciiChar.Char(char.toString(), char.isFullWidth()))
                    if (char.isFullWidth()) {
                        offset++
                        matrix.set(x + offset, y, AsciiChar.FullWidthSpace)
                    }
                    x++
                }
            }
        }
    }
    return matrix
}

private fun toAsciiMatrix(line: Line): Matrix<AsciiChar> {
    val matrix = Matrix.init<AsciiChar>(line.width, line.height, AsciiChar.Transparent)
    val landscape = line.width > line.height
    val boundingList = line.toPointList(landscape)
        .toBoundingPointsList()
        .map(::toBounding)
    val boxDrawing = boundingList.complementStroke() + boundingList.last()

    boxDrawing.forEach { (point, boundingType) ->
        matrix.set(point.x, point.y, AsciiChar.Bounding(boundingType))
    }

    val point = boxDrawing.last().point
    val triangle = if (line.width > line.height) {
        if (point.x == 0) {
            Triangle.LEFT_TRIANGLE
        } else {
            Triangle.RIGHT_TRIANGLE
        }
    } else {
        if (point.y == 0) {
            Triangle.UP_TRIANGLE
        } else {
            Triangle.DOWN_TRIANGLE
        }
    }
    matrix.set(point.x, point.y, AsciiChar.Char(triangle.char))
    return matrix
}

private fun Rect.toPointList(): List<Point> {
    val points = arrayOf(
        Point(0, 0),
        Point(width - 1, 0),
        Point(width - 1, height - 1),
        Point(0, height - 1)
    )
    return listOf(
        points.last(),
        *points,
        points.first()
    )
}

private fun Line.toPointList(landscape: Boolean): List<Point> {
    val split = if (landscape) {
        ceil(width / 2f).toInt() - 1
    } else {
        ceil(height / 2f).toInt() - 1
    }
    val (p2, p3) = if (landscape) {
        Point(split, 0) to Point(split, height - 1)
    } else {
        Point(0, split) to Point(width - 1, split)
    }
    val offsetStart = start - Point(left, top)
    val offsetEnd = end - Point(left, top)

    return if (landscape) {
        if ((start.x > end.x && start.y > end.y) || (start.x < end.x && start.y > end.y)) {
            listOf(offsetStart, p3, p2, offsetEnd)
        } else {
            listOf(offsetStart, p2, p3, offsetEnd)
        }
    } else {
        if ((start.x > end.x && start.y > end.y) || (start.x > end.x && start.y < end.y)) {
            listOf(offsetStart, p3, p2, offsetEnd)
        } else {
            listOf(offsetStart, p2, p3, offsetEnd)
        }
    }
}

private fun List<Point>.toBoundingPointsList(): List<BoundingPoints> =
    List(this.size) { index ->
        val a = index - 1
        val b = index + 2
        when {
            a < 0 -> this.slice(0..1)
            b > this.size -> this.takeLast(2).reversed()
            else -> this.slice(a..a + 2)
        }.toBoundingPoints()
    }

private fun toBounding(boundingPoints: BoundingPoints): Bounding = when (boundingPoints) {
    is Edge -> Bounding(
        boundingPoints.value.second,
        boundingPoints.toBoundingType()
    )
    is Side -> Bounding(
        boundingPoints.value.first,
        boundingPoints.toBoundingType()
    )
}

private fun List<Bounding>.complementStroke(): List<Bounding> =
    this.toMutableList()
        .zipWithNext()
        .flatMap { (a, b) ->
            mutableListOf(a) + when {
                a.point.x == b.point.x -> (a.point.y between b.point.y)
                    .map { y ->
                        Bounding(Point(a.point.x, y), BoundingType.VERTICAL)
                    }
                a.point.y == b.point.y -> (a.point.x between b.point.x)
                    .map { x ->
                        Bounding(Point(x, a.point.y), BoundingType.HORIZONTAL)
                    }
                else -> throw IllegalArgumentException("Illegal corner pair")
            }
        }

private fun List<Point>.toBoundingPoints(): BoundingPoints = when (this.size) {
    2 -> Side(
        Pair(this[0], this[1])
    )
    3 -> Edge(
        Triple(this[0], this[1], this[2])
    )
    else -> throw IllegalArgumentException("List size is 2 or 3")
}

private fun Edge.toBoundingType(
    reversed: Boolean = false
): BoundingType {
    val (a, b, c) = this.value
    return when {
        a.x == b.x && b.x == c.x -> BoundingType.VERTICAL
        a.y == b.y && b.y == c.y -> BoundingType.HORIZONTAL
        a.y == b.y && b.x == c.x && a.x < b.x && b.y < c.y -> BoundingType.DOWN_AND_LEFT
        a.y == b.y && b.x == c.x && a.x > b.x && b.y < c.y -> BoundingType.DOWN_AND_RIGHT
        a.x == b.x && b.y == c.y && a.y < b.y && b.x > c.x -> BoundingType.UP_AND_LEFT
        a.x == b.x && b.y == c.y && a.y < b.y && b.x < c.x -> BoundingType.UP_AND_RIGHT
        !reversed -> Edge(Triple(c, b, a)).toBoundingType(true)
        else -> throw IllegalArgumentException("Illegal point combination in Corner")
    }
}

private fun Side.toBoundingType(): BoundingType {
    val (a, b) = this.value
    return if (a.x == b.x) {
        BoundingType.VERTICAL
    } else {
        BoundingType.HORIZONTAL
    }
}

private infix fun Int.between(other: Int): IntProgression = if (this < other) {
    this + 1 until other
} else {
    ( other + 1 until this).reversed()
}

//https://stackoverflow.com/a/62844233
private fun Char.isFullWidth(): Boolean = when (this) {
    '\u2329','\u232A','\u23F0','\u23F3','\u267F','\u2693','\u26A1','\u26CE','\u26D4','\u26EA','\u26F5',
    '\u26FA','\u26FD','\u2705','\u2728','\u274C','\u274E','\u2757','\u27B0','\u27BF','\u2B50','\u2B55',
    '\u3000','\u3004','\u3005','\u3006','\u3007','\u3008','\u3009','\u300A','\u300B','\u300C','\u300D',
    '\u300E','\u300F','\u3010','\u3011','\u3014','\u3015','\u3016','\u3017','\u3018','\u3019','\u301A',
    '\u301B','\u301C','\u301D','\u3020','\u3030','\u303B','\u303C','\u303D','\u303E','\u309F','\u30A0',
    '\u30FB','\u30FF','\u3250','\uA015','\uFE17','\uFE18','\uFE19','\uFE30','\uFE35','\uFE36','\uFE37',
    '\uFE38','\uFE39','\uFE3A','\uFE3B','\uFE3C','\uFE3D','\uFE3E','\uFE3F','\uFE40','\uFE41','\uFE42',
    '\uFE43','\uFE44','\uFE47','\uFE48','\uFE58','\uFE59','\uFE5A','\uFE5B','\uFE5C','\uFE5D','\uFE5E',
    '\uFE62','\uFE63','\uFE68','\uFE69','\uFF04','\uFF08','\uFF09','\uFF0A','\uFF0B','\uFF0C','\uFF0D',
    '\uFF3B','\uFF3C','\uFF3D','\uFF3E','\uFF3F','\uFF40','\uFF5B','\uFF5C','\uFF5D','\uFF5E','\uFF5F',
    '\uFF60','\uFFE2','\uFFE3','\uFFE4',
    in '\u1100'..'\u115F',in '\u231A'..'\u231B',in '\u23E9'..'\u23EC',in '\u25FD'..'\u25FE',
    in '\u2614'..'\u2615',in '\u2648'..'\u2653',in '\u26AA'..'\u26AB',in '\u26BD'..'\u26BE',
    in '\u26C4'..'\u26C5',in '\u26F2'..'\u26F3',in '\u270A'..'\u270B',in '\u2753'..'\u2755',
    in '\u2795'..'\u2797',in '\u2B1B'..'\u2B1C',in '\u2E80'..'\u2E99',in '\u2E9B'..'\u2EF3',
    in '\u2F00'..'\u2FD5',in '\u2FF0'..'\u2FFB',in '\u3001'..'\u3003',in '\u3012'..'\u3013',
    in '\u301E'..'\u301F',in '\u3021'..'\u3029',in '\u302A'..'\u302D',in '\u302E'..'\u302F',
    in '\u3031'..'\u3035',in '\u3036'..'\u3037',in '\u3038'..'\u303A',in '\u3041'..'\u3096',
    in '\u3099'..'\u309A',in '\u309B'..'\u309C',in '\u309D'..'\u309E',in '\u30A1'..'\u30FA',
    in '\u30FC'..'\u30FE',in '\u3105'..'\u312F',in '\u3131'..'\u318E',in '\u3190'..'\u3191',
    in '\u3192'..'\u3195',in '\u3196'..'\u319F',in '\u31A0'..'\u31BF',in '\u31C0'..'\u31E3',
    in '\u31F0'..'\u31FF',in '\u3200'..'\u321E',in '\u3220'..'\u3229',in '\u322A'..'\u3247',
    in '\u3251'..'\u325F',in '\u3260'..'\u327F',in '\u3280'..'\u3289',in '\u328A'..'\u32B0',
    in '\u32B1'..'\u32BF',in '\u32C0'..'\u32FF',in '\u3300'..'\u33FF',in '\u3400'..'\u4DBF',
    in '\u4E00'..'\u9FFC',in '\u9FFD'..'\u9FFF',in '\uA000'..'\uA014',in '\uA016'..'\uA48C',
    in '\uA490'..'\uA4C6',in '\uA960'..'\uA97C',in '\uAC00'..'\uD7A3',in '\uF900'..'\uFA6D',
    in '\uFA6E'..'\uFA6F',in '\uFA70'..'\uFAD9',in '\uFADA'..'\uFAFF',in '\uFE10'..'\uFE16',
    in '\uFE31'..'\uFE32',in '\uFE33'..'\uFE34',in '\uFE45'..'\uFE46',in '\uFE49'..'\uFE4C',
    in '\uFE4D'..'\uFE4F',in '\uFE50'..'\uFE52',in '\uFE54'..'\uFE57',in '\uFE5F'..'\uFE61',
    in '\uFE64'..'\uFE66',in '\uFE6A'..'\uFE6B',in '\uFF01'..'\uFF03',in '\uFF05'..'\uFF07',
    in '\uFF0E'..'\uFF0F',in '\uFF10'..'\uFF19',in '\uFF1A'..'\uFF1B',in '\uFF1C'..'\uFF1E',
    in '\uFF1F'..'\uFF20',in '\uFF21'..'\uFF3A',in '\uFF41'..'\uFF5A',in '\uFFE0'..'\uFFE1',
    in '\uFFE5'..'\uFFE6' -> true
    else -> false
}
