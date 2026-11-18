package ViewModel.Chat

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import ViewModel.Keybinds.Keybinds
import ViewModel.ViewModel

object Commands {
    @JvmStatic fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommands.literal(ViewModel.MOD_ID).executes {
                // The chat screen closes after the command runs, so open on the next tick.
                Keybinds.openMenuNextTick = true
                1
            })
        }
    }
}
