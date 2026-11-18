package ViewModel.Gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.ARGB
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Shape drawing on top of vanilla's [GuiGraphicsExtractor].
 *
 * Inputs are GUI-scaled coordinates (floats allowed). Internally everything is rasterised in physical
 * pixels by un-applying the GUI scale, so rounded corners and strokes stay crisp at any scale.
 * Rounded edges get one partially transparent pixel per row for horizontal anti-aliasing.
 */
object Draw {
    enum class Align { LEFT, CENTER, RIGHT }

    private inline fun physical(g: GuiGraphicsExtractor, block: (scale: Float) -> Unit) {
        val scale = Minecraft.getInstance().window.guiScale.toFloat()
        g.pose().pushMatrix()
        g.pose().scale(1f / scale, 1f / scale)
        try {
            block(scale)
        } finally {
            g.pose().popMatrix()
        }
    }

    private fun withAlpha(color: Int, coverage: Float) =
        ARGB.color((ARGB.alpha(color) * coverage).roundToInt().coerceIn(0, 255), color)

    fun rect(g: GuiGraphicsExtractor, x: Float, y: Float, width: Float, height: Float, color: Int) =
        roundedRect(g, x, y, width, height, 0f, color)

    /** Rounded rectangle with an optional vertical gradient (`top` to `bottom`). */
    fun roundedRect(
        g: GuiGraphicsExtractor, x: Float, y: Float, width: Float, height: Float,
        radius: Float, top: Int, bottom: Int = top,
    ) = physical(g) { s ->
        val left = (x * s).roundToInt()
        val right = ((x + width) * s).roundToInt()
        val y0 = (y * s).roundToInt()
        val y1 = ((y + height) * s).roundToInt()
        val w = right - left
        val h = y1 - y0
        if (w <= 0 || h <= 0) return@physical
        val r = min(radius * s, min(w, h) / 2f).coerceAtLeast(0f)
        val cornerRows = ceil(r).toInt().coerceAtMost(h / 2)

        fun rowColor(row: Int) = if (top == bottom || h <= 1) top else ARGB.srgbLerp(row / (h - 1f), top, bottom)

        fun cornerRow(row: Int, distance: Float) {
            val inset = r - sqrt(max(0f, r * r - distance * distance))
            val solid = ceil(inset).toInt()
            val color = rowColor(row)
            val py = y0 + row
            if (right - solid > left + solid) g.fill(left + solid, py, right - solid, py + 1, color)
            val edge = floor(inset).toInt()
            val coverage = 1f - (inset - edge)
            if (edge < solid && coverage > 0.02f) {
                val faded = withAlpha(color, coverage)
                g.fill(left + edge, py, left + edge + 1, py + 1, faded)
                g.fill(right - edge - 1, py, right - edge, py + 1, faded)
            }
        }

        for (row in 0 until cornerRows) cornerRow(row, r - (row + 0.5f))
        val middleTop = y0 + cornerRows
        val middleBottom = y1 - cornerRows
        if (middleBottom > middleTop) {
            if (top == bottom) g.fill(left, middleTop, right, middleBottom, top)
            else g.fillGradient(left, middleTop, right, middleBottom, rowColor(cornerRows), rowColor(h - cornerRows - 1))
        }
        for (row in h - cornerRows until h) cornerRow(row, (row + 0.5f) - (h - r))
    }

    fun circle(g: GuiGraphicsExtractor, centerX: Float, centerY: Float, radius: Float, color: Int) =
        roundedRect(g, centerX - radius, centerY - radius, radius * 2, radius * 2, radius, color)

    /** A 1px border with a filled body, as used by every control. */
    fun bordered(g: GuiGraphicsExtractor, x: Float, y: Float, width: Float, height: Float, radius: Float, border: Int, fill: Int) {
        roundedRect(g, x, y, width, height, radius, border)
        roundedRect(g, x + 1, y + 1, width - 2, height - 2, max(0f, radius - 1), fill)
    }

    fun panel(g: GuiGraphicsExtractor, x: Float, y: Float, width: Float, height: Float) {
        roundedRect(g, x + 2, y + 4, width, height, Theme.PANEL_RADIUS, Theme.PANEL_SHADOW)
        bordered(g, x, y, width, height, Theme.PANEL_RADIUS, Theme.PANEL_BORDER, Theme.PANEL)
    }

    /** Stroked polyline made of square dabs; points are GUI coordinates. */
    fun polyline(g: GuiGraphicsExtractor, points: List<Pair<Float, Float>>, thickness: Float, color: Int) = physical(g) { s ->
        val t = max(1, (thickness * s).roundToInt())
        fun dab(px: Float, py: Float) {
            val x0 = (px * s - t / 2f).roundToInt()
            val y0 = (py * s - t / 2f).roundToInt()
            g.fill(x0, y0, x0 + t, y0 + t, color)
        }
        for (i in 0 until points.size - 1) {
            val (ax, ay) = points[i]
            val (bx, by) = points[i + 1]
            val steps = max(1, ceil(hypot((bx - ax) * s, (by - ay) * s) * 2).toInt())
            for (step in 0..steps) {
                val f = step / steps.toFloat()
                dab(ax + (bx - ax) * f, ay + (by - ay) * f)
            }
        }
    }

    fun line(g: GuiGraphicsExtractor, x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) =
        polyline(g, listOf(x1 to y1, x2 to y2), thickness, color)

    /** Chevron centred vertically on [y], spanning [size] horizontally from [x]. */
    fun chevron(g: GuiGraphicsExtractor, x: Float, y: Float, size: Float, up: Boolean, color: Int) {
        val d = if (up) -1f else 1f
        polyline(g, listOf(x to y - d * size * .25f, x + size / 2 to y + d * size * .25f, x + size to y - d * size * .25f), 1.2f, color)
    }

    /** Counter-clockwise "reset" arrow inside a [size] x [size] box (24-unit icon grid). */
    fun resetIcon(g: GuiGraphicsExtractor, x: Float, y: Float, size: Float, color: Int) {
        val u = size / 24f
        val cx = x + 12 * u
        val cy = y + 12 * u
        val r = 9 * u
        val arc = ArrayList<Pair<Float, Float>>()
        arc += (x + 2 * u) to (y + 10 * u)
        var degrees = 225f
        while (degrees <= 524f) {
            val radians = Math.toRadians(degrees.toDouble())
            arc += (cx + r * cos(radians).toFloat()) to (cy + r * sin(radians).toFloat())
            degrees += 6f
        }
        val stroke = 2 * u
        polyline(g, arc, stroke, color)
        line(g, x + 2 * u, y + 10 * u, x + 2 * u, y + 4 * u, stroke, color)
        line(g, x + 2 * u, y + 10 * u, x + 8 * u, y + 10 * u, stroke, color)
    }

    /**
     * Vanilla-font text, vertically centred on [centerY]. [size] is a nominal pixel height (9 = vanilla);
     * text wider than [maxWidth] is shrunk to fit.
     */
    fun text(
        g: GuiGraphicsExtractor, text: String, x: Float, centerY: Float, color: Int,
        align: Align = Align.LEFT, size: Float = 9f, maxWidth: Float = Float.MAX_VALUE,
    ) {
        val font = Minecraft.getInstance().font
        val raw = font.width(text).toFloat()
        if (raw <= 0f) return
        val scale = min(size / 9f, maxWidth / raw)
        val width = raw * scale
        val left = when (align) {
            Align.LEFT -> x
            Align.CENTER -> x - width / 2
            Align.RIGHT -> x - width
        }
        g.pose().pushMatrix()
        g.pose().translate(left, centerY - (font.lineHeight - 1) / 2f * scale)
        g.pose().scale(scale, scale)
        g.text(font, text, 0, 0, color, false)
        g.pose().popMatrix()
    }
}
