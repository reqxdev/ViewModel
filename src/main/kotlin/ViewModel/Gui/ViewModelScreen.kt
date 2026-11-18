package ViewModel.Gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import ViewModel.Config.Profiles

/** The ViewModel editor: profile management on the left, live sliders and toggles in the centre. */
class ViewModelScreen(private val parent: Screen?) : Screen(Component.translatable("viewmodel.gui.title")) {
    private class SliderRow(val option: Option, val slider: UiSlider, val labelX: Int, val labelY: Int, val valueRight: Int)

    private val sliders = mutableListOf<SliderRow>()
    private val toggles = mutableListOf<UiToggle>()
    private lateinit var layout: Layout
    private lateinit var dropdown: ProfileDropdown
    private var status = ""
    private var statusColor = Theme.ACCENT
    private var statusTicks = 0

    override fun init() {
        sliders.clear()
        toggles.clear()
        layout = Layout.centered(width, height)
        buildProfiles()
        buildEditor()
    }

    private fun buildProfiles() {
        val card = layout.profiles.inset(PROFILE_PADDING)
        dropdown = addRenderableWidget(ProfileDropdown(card.x, card.y + 18, card.width, Profiles.names, Profiles.activeIndex, ::selectProfile))

        val gap = 3
        val buttonWidth = (card.width - gap * 2) / 3
        val y = dropdown.y + dropdown.height + 5
        addRenderableWidget(UiButton(card.x, y, buttonWidth, 17, Component.translatable("viewmodel.gui.create"), UiButton.Style.PRIMARY, ::createProfile))
        addRenderableWidget(UiButton(card.x + buttonWidth + gap, y, buttonWidth, 17, Component.translatable("viewmodel.gui.rename"), UiButton.Style.SECONDARY, ::renameProfile))
        addRenderableWidget(UiButton(card.x + (buttonWidth + gap) * 2, y, buttonWidth, 17, Component.translatable("viewmodel.gui.delete"), UiButton.Style.DANGER, ::deleteProfile))
    }

    private fun buildEditor() {
        val card = layout.editor.inset(EDITOR_PADDING)
        var y = layout.editor.y + 38
        for (option in Option.entries) {
            val sliderX = card.x + 27
            val slider = addRenderableWidget(UiSlider(sliderX, y, card.width - 27, option.min, option.max, option.step, option.value) { option.value = it })
            addRenderableWidget(ResetIconButton(card.x, y - 2) { slider.reset(option.baseline) })
            sliders += SliderRow(option, slider, sliderX, y - 4, card.right)
            y += 20
        }

        val resetY = y + 1
        addRenderableWidget(UiButton(card.x, resetY, card.width, 16, Component.translatable("viewmodel.gui.reset"), UiButton.Style.WIDE, ::resetProfile))

        val toggleGap = 8
        val toggleWidth = (card.width - toggleGap * 2) / 3
        val toggleY = resetY + 28
        fun toggle(column: Int, key: String, get: () -> Boolean, set: (Boolean) -> Unit) {
            toggles += addRenderableWidget(UiToggle(card.x + (toggleWidth + toggleGap) * column, toggleY, toggleWidth, Component.translatable(key), get, set))
        }
        toggle(0, "viewmodel.gui.no_swing", { Profiles.current.noSwing }) { Profiles.current.noSwing = it }
        toggle(1, "viewmodel.gui.scale_swing", { Profiles.current.scaleSwing }) { Profiles.current.scaleSwing = it }
        toggle(2, "viewmodel.gui.no_equip", { Profiles.current.skipEquipAnimation }) { Profiles.current.skipEquipAnimation = it }
    }

    private fun createProfile() {
        minecraft.gui.setScreen(ProfileNameScreen(this, Component.literal("Create profile"), "") { name ->
            if (name.isBlank()) return@ProfileNameScreen Component.literal("Name cannot be empty")
            setStatus("Created ${Profiles.create(name).name}")
            null
        })
    }

    private fun renameProfile() {
        minecraft.gui.setScreen(ProfileNameScreen(this, Component.literal("Rename profile"), Profiles.active.name) { name ->
            if (name.isBlank()) return@ProfileNameScreen Component.literal("Name cannot be empty")
            if (!Profiles.renameActive(name)) return@ProfileNameScreen Component.literal("Name already exists")
            setStatus("Renamed to ${Profiles.active.name}")
            null
        })
    }

    private fun deleteProfile() {
        if (!Profiles.deleteActive()) {
            setStatus("Cannot delete the last profile", Theme.ERROR)
            return
        }
        setStatus("Profile deleted")
        rebuildWidgets()
    }

    private fun selectProfile(index: Int) {
        if (index == Profiles.activeIndex) return
        Profiles.select(index)
        setStatus("Switched to ${Profiles.active.name}")
        rebuildWidgets()
    }

    private fun resetProfile() {
        Profiles.current.resetToBaseline()
        sliders.forEach { it.slider.sync(it.option.value) }
        toggles.forEach { it.refresh() }
        setStatus("Profile reset")
    }

    private fun setStatus(text: String, color: Int = Theme.ACCENT) {
        status = text
        statusColor = color
        statusTicks = 80
    }

    /** A light tint only: no blur, so the held item stays visible as a live preview. */
    override fun extractBackground(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        g.fill(0, 0, width, height, Theme.BACKDROP)
    }

    override fun extractRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        Draw.panel(g, layout.profiles.x.toFloat(), layout.profiles.y.toFloat(), layout.profiles.width.toFloat(), layout.profiles.height.toFloat())
        Draw.panel(g, layout.editor.x.toFloat(), layout.editor.y.toFloat(), layout.editor.width.toFloat(), layout.editor.height.toFloat())
        drawLabels(g)

        // While the list is open, nothing underneath it should react to hover.
        val blocked = dropdown.isOpen && dropdown.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        super.extractRenderState(g, if (blocked) -1 else mouseX, if (blocked) -1 else mouseY, delta)

        if (dropdown.isOpen) {
            g.nextStratum()
            dropdown.extractOverlay(g, mouseX, mouseY)
        }
    }

    private fun drawLabels(g: GuiGraphicsExtractor) {
        val profilesCard = layout.profiles.inset(PROFILE_PADDING)
        val editorCard = layout.editor.inset(EDITOR_PADDING)
        Draw.text(g, Component.translatable("viewmodel.gui.configs").string.uppercase(), profilesCard.x.toFloat(), profilesCard.y + 5f, Theme.TEXT)
        Draw.text(g, Profiles.active.name.uppercase(), editorCard.x.toFloat(), layout.editor.y + 16f, Theme.TEXT, maxWidth = editorCard.width.toFloat())

        for (row in sliders) {
            Draw.text(g, row.option.label, row.labelX.toFloat(), row.labelY.toFloat(), Theme.TEXT)
            Draw.text(g, row.slider.formattedValue(), row.valueRight.toFloat(), row.labelY.toFloat(), Theme.TEXT, Draw.Align.RIGHT)
        }
        if (statusTicks > 0 && status.isNotEmpty()) {
            Draw.text(g, status, profilesCard.x.toFloat(), layout.profiles.bottom + 8f, statusColor, size = 8f)
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (dropdown.isOpen && !dropdown.isMouseOver(event.x(), event.y())) {
            dropdown.close()
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun tick() {
        super.tick()
        if (statusTicks > 0) statusTicks--
    }

    override fun onClose() = minecraft.gui.setScreen(parent)
    override fun removed() = Profiles.save()

    private companion object {
        const val PROFILE_PADDING = 6
        const val EDITOR_PADDING = 12
    }
}
