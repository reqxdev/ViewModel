package ViewModel.Gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import ViewModel.Config.Profiles

/** Text prompt for creating/renaming a profile. [submit] returns an error message, or null on success. */
class ProfileNameScreen(
    private val parent: Screen, title: Component, private val initialValue: String,
    private val submit: (String) -> Component?,
) : Screen(title) {
    private lateinit var field: EditBox
    private var error: Component = Component.empty()

    override fun init() {
        field = addRenderableWidget(EditBox(font, width / 2 - 100, height / 2 - 10, 200, 20, Component.empty()))
        field.setMaxLength(Profiles.MAX_NAME_LENGTH)
        field.value = initialValue
        field.setResponder { error = Component.empty() }
        setInitialFocus(field)
        addRenderableWidget(Button.builder(Component.literal("Confirm")) { confirm() }
            .bounds(width / 2 - 100, height / 2 + 22, 96, 20).build())
        addRenderableWidget(Button.builder(Component.literal("Cancel")) { onClose() }
            .bounds(width / 2 + 4, height / 2 + 22, 96, 20).build())
    }

    private fun confirm() {
        val result = submit(field.value.trim())
        if (result == null) minecraft.gui.setScreen(parent) else error = result
    }

    override fun onClose() = minecraft.gui.setScreen(parent)

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.isConfirmation) {
            confirm()
            return true
        }
        return super.keyPressed(event)
    }

    override fun extractBackground(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        g.fill(0, 0, width, height, Theme.BACKDROP)
    }

    override fun extractRenderState(g: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        Draw.panel(g, width / 2f - 112, height / 2f - 56, 224f, 106f)
        g.centeredText(font, title, width / 2, height / 2 - 42, Theme.TEXT)
        super.extractRenderState(g, mouseX, mouseY, delta)
        if (error.string.isNotEmpty()) g.centeredText(font, error, width / 2, height / 2 + 50 + 6, Theme.ERROR)
    }
}
