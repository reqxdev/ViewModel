package ViewModel

import net.fabricmc.api.ClientModInitializer
import ViewModel.Chat.Commands
import ViewModel.Config.Profiles
import ViewModel.Keybinds.Keybinds

class ViewModel : ClientModInitializer {
    override fun onInitializeClient() {
        Profiles.load()
        Commands.register()
        Keybinds.register()
    }

    companion object {
        const val MOD_ID = "viewmodel"
    }
}
