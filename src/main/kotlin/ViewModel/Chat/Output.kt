package ViewModel.Chat

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import ViewModel.ViewModel

object Output {
    const val PREFIX = "ViewModel >> "
    const val MARKER = ViewModel.MOD_ID + ":chat"
    val FIRST_COLOR = 0xFF3D1017.toInt()
    val SECOND_COLOR = 0xFFFF4655.toInt()

    fun message(client: Minecraft, message: Component) {
        client.player?.sendSystemMessage(Component.empty()
            .append(Component.literal(PREFIX).setStyle(Style.EMPTY.withColor(FIRST_COLOR).withInsertion(MARKER)))
            .append(message))
    }

    fun toggle(client: Minecraft, feature: String, enabled: Boolean) {
        message(client, Component.literal("$feature ").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(if (enabled) "ENABLED" else "DISABLED")
                .withStyle(if (enabled) ChatFormatting.GREEN else ChatFormatting.RED, ChatFormatting.BOLD)))
    }
}
