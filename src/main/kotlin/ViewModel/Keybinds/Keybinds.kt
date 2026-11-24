package ViewModel.Keybinds

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import ViewModel.Gui.ViewModelScreen
import ViewModel.ViewModel

object Keybinds {
    @JvmField var openMenuNextTick = false
    @JvmField val CATEGORY = KeyMapping.Category(Identifier.fromNamespaceAndPath(ViewModel.MOD_ID, ViewModel.MOD_ID))
    @JvmField val OPEN_MENU = KeyMapping(
        "key.${ViewModel.MOD_ID}.open_menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, CATEGORY
    )

    @JvmStatic fun register() {
        KeyMappingHelper.registerKeyMapping(OPEN_MENU)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            var open = openMenuNextTick
            openMenuNextTick = false
            while (OPEN_MENU.consumeClick()) open = true
            if (open) openMenu(client)
        }
    }

    private fun openMenu(client: Minecraft) {
        if (client.player == null) return
        client.gui.setScreen(ViewModelScreen(client.gui.screen()))
    }
}
