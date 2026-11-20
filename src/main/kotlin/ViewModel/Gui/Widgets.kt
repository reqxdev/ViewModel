package ViewModel.Gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import java.text.DecimalFormat
import kotlin.math.roundToLong
import kotlin.math.sign

abstract class UiWidget(x: Int, y: Int, width: Int, height: Int, label: Component) :
    AbstractWidget(x, y, width, height, label) {
    protected val fx get() = x.toFloat()
    protected val fy get() = y.toFloat()
    protected val fw get() = width.toFloat()
    protected val fh get() = height.toFloat()

    override fun updateWidgetNarration(output: NarrationElementOutput) = defaultButtonNarrationText(output)
}

class UiButton(
    x: Int, y: Int, width: Int, height: Int, label: Component,
    private val style: Style, private val action: () -> Unit,
) : UiWidget(x, y, width, height, label) {
    enum class Style { PRIMARY, SECONDARY, DANGER, WIDE }

    override fun extractWidgetRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = isHovered
        if (style == Style.WIDE) {
            Draw.roundedRect(g, fx, fy, fw, fh, 6f, Theme.CONTROL_BORDER)
            Draw.roundedRect(g, fx + 1, fy + 1, fw - 2, fh - 2, 5f,
                if (hovered) 0xFF424243.toInt() else 0xFF373738.toInt(),
                if (hovered) 0xFF323234.toInt() else 0xFF2C2C2D.toInt())
        } else {
            val fill = when (style) {
                Style.PRIMARY -> if (hovered) Theme.ACCENT else Theme.ACCENT_SOFT
                Style.DANGER -> if (hovered) Theme.DANGER_HOVER else Theme.DANGER
                else -> if (hovered) Theme.SURFACE_HOVER else Theme.SURFACE
            }
            Draw.bordered(g, fx, fy, fw, fh, Theme.CONTROL_RADIUS, Theme.CONTROL_BORDER, fill)
        }
        Draw.text(g, message.string, fx + fw / 2, fy + fh / 2, Theme.TEXT, Draw.Align.CENTER,
            if (style == Style.WIDE) 9f else 8f, fw - 4)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) = action()
}

class ResetIconButton(x: Int, y: Int, private val action: () -> Unit) :
    UiWidget(x, y, 16, 16, Component.literal("Reset")) {
    override fun extractWidgetRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        Draw.bordered(g, fx, fy, fw, fh, 2f, Theme.CONTROL_BORDER, if (isHovered) Theme.ACCENT_HOVER else Theme.ACCENT)
        Draw.resetIcon(g, fx + 2.5f, fy + 2.5f, fw - 5, Theme.WHITE)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) = action()
}

class UiToggle(
    x: Int, y: Int, width: Int, label: Component,
    private val source: () -> Boolean, private val changed: (Boolean) -> Unit,
) : UiWidget(x, y, width, SWITCH_HEIGHT, label) {
    private var value = source()

    override fun extractWidgetRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val switchX = fx + fw - SWITCH_WIDTH
        Draw.roundedRect(g, switchX, fy, SWITCH_WIDTH.toFloat(), fh, SWITCH_HEIGHT / 2f, if (value) Theme.ACCENT else Theme.SWITCH_OFF)
        val knobX = if (value) switchX + SWITCH_WIDTH - KNOB_SIZE + 1 else switchX - 1
        Draw.circle(g, knobX + KNOB_SIZE / 2f, fy + fh / 2, KNOB_SIZE / 2f, if (isHovered) 0xFFFFFFFF.toInt() else Theme.WHITE)
        Draw.text(g, message.string, fx, fy + fh / 2, Theme.TEXT, size = 8f, maxWidth = fw - SWITCH_WIDTH - 3)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        value = !value
        changed(value)
    }

    fun refresh() { value = source() }

    private companion object {
        const val SWITCH_WIDTH = 28
        const val SWITCH_HEIGHT = 10
        const val KNOB_SIZE = 12
    }
}

class UiSlider(
    x: Int, y: Int, width: Int,
    private val min: Double, private val max: Double, private val step: Double,
    initial: Double, private val changed: (Double) -> Unit,
) : AbstractSliderButton(x, y, width, 12, Component.empty(), 0.0) {
    init { sync(initial) }

    override fun updateMessage() = setMessage(Component.empty())

    override fun applyValue() {
        val actual = snap(Mth.lerp(value, min, max))
        sync(actual)
        changed(actual)
    }

    override fun extractWidgetRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val trackY = y + height / 2f - 2
        val knobX = (x + value * (width - 8)).toFloat()
        Draw.roundedRect(g, x.toFloat(), trackY, width.toFloat(), 4f, 2f, Theme.TRACK)
        Draw.roundedRect(g, x.toFloat(), trackY, knobX + 4 - x, 4f, 2f, Theme.ACCENT)
        Draw.circle(g, knobX + 4, trackY + 2, 4f, if (isHovered || isFocused) 0xFFFFFFFF.toInt() else Theme.WHITE)
    }

    fun reset(actual: Double) {
        sync(actual)
        changed(actual)
    }

    fun sync(actual: Double) { value = Mth.clamp((actual - min) / (max - min), 0.0, 1.0) }
    fun formattedValue(): String = FORMAT.format(Mth.lerp(value, min, max))

    private fun snap(actual: Double) = if (step <= 0) actual else (actual / step).roundToLong() * step

    private companion object {
        val FORMAT = DecimalFormat("+0.00;-0.00")
    }
}

class ProfileDropdown(
    x: Int, y: Int, width: Int, entries: List<String>, selectedIndex: Int,
    private val selected: (Int) -> Unit,
) : UiWidget(x, y, width, ROW_HEIGHT, Component.literal("Profiles")) {
    private val entries = entries.toList()
    private val selectedIndex = if (entries.isEmpty()) -1 else selectedIndex.coerceIn(0, entries.lastIndex)
    private var scrollOffset = (this.selectedIndex - MAX_VISIBLE + 1).coerceIn(0, maxOf(0, entries.size - MAX_VISIBLE))
    var isOpen = false
        private set

    private val visibleRows get() = minOf(MAX_VISIBLE, entries.size)
    private val overlayY get() = y + height + 2

    override fun extractWidgetRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        val headerHovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
        Draw.bordered(g, fx, fy, fw, fh, Theme.CONTROL_RADIUS, Theme.CONTROL_BORDER,
            if (headerHovered) Theme.SURFACE_HOVER else Theme.SURFACE)
        Draw.chevron(g, fx + fw - 15, fy + fh / 2, 6f, isOpen, Theme.ACCENT)
        Draw.text(g, label(selectedIndex), fx + 7, fy + fh / 2, Theme.TEXT, size = 8.5f, maxWidth = fw - 26)
    }

    fun extractOverlay(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (!isOpen) return
        val top = overlayY.toFloat()
        val listHeight = visibleRows * fh + 2
        Draw.bordered(g, fx, top, fw, listHeight, 2f, Theme.CONTROL_BORDER, Theme.SURFACE)
        val hoveredRow = rowAt(mouseX.toDouble(), mouseY.toDouble())
        for (row in 0 until visibleRows) {
            val index = scrollOffset + row
            val rowY = top + 1 + row * fh
            if (index == selectedIndex || row == hoveredRow) Draw.rect(g, fx + 1, rowY, fw - 2, fh, Theme.SURFACE_HOVER)
            if (index == selectedIndex) Draw.rect(g, fx + 1, rowY, 2f, fh, Theme.ACCENT)
            Draw.text(g, label(index), fx + 7, rowY + fh / 2, Theme.TEXT, size = 8.5f, maxWidth = fw - 12)
        }
        if (entries.size > MAX_VISIBLE) {
            val trackHeight = listHeight - 4
            val thumbHeight = trackHeight * MAX_VISIBLE / entries.size
            val thumbY = top + 2 + (trackHeight - thumbHeight) * scrollOffset / (entries.size - MAX_VISIBLE)
            Draw.roundedRect(g, fx + fw - 4, thumbY, 2f, thumbHeight, 1f, Theme.TEXT_MUTED)
        }
    }

    private fun rowAt(mouseX: Double, mouseY: Double): Int {
        if (mouseX < x || mouseX >= x + width) return -1
        val offset = mouseY - overlayY - 1
        if (offset < 0) return -1
        val row = (offset / height).toInt()
        return if (row < visibleRows) row else -1
    }

    private fun label(index: Int) = entries.getOrNull(index) ?: "None"

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (!isOpen) {
            isOpen = true
            return
        }
        val row = rowAt(event.x(), event.y())
        if (row >= 0) selected(scrollOffset + row)
        isOpen = false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontal: Double, vertical: Double): Boolean {
        if (!isOpen || !isMouseOver(mouseX, mouseY)) return false
        scrollOffset = (scrollOffset - vertical.sign.toInt()).coerceIn(0, maxOf(0, entries.size - MAX_VISIBLE))
        return true
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        val bottom = if (isOpen) overlayY + visibleRows * height + 2 else y + height
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < bottom
    }

    fun close() { isOpen = false }

    private companion object {
        const val MAX_VISIBLE = 5
        const val ROW_HEIGHT = 16
    }
}
