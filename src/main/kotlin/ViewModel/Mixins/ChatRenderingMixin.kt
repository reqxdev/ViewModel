package ViewModel.Mixins

import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.client.multiplayer.chat.GuiMessage
import net.minecraft.util.ARGB
import net.minecraft.util.FormattedCharSequence
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import ViewModel.Chat.Output
import kotlin.math.abs

@Mixin(GuiMessage.Line::class)
abstract class ChatRenderingMixin {
    @ModifyReturnValue(method = ["content()Lnet/minecraft/util/FormattedCharSequence;"], at = [At("RETURN")])
    private fun `viewmodel$animate`(text: FormattedCharSequence): FormattedCharSequence =
        FormattedCharSequence { sink ->
            val time = (System.currentTimeMillis() % 2500L) / 2500f
            text.accept { index, style, codePoint ->
                val animated = if (Output.MARKER == style.insertion) {
                    val blend = 1 - abs((index.toFloat() / Output.PREFIX.length - time + 1) % 1 * 2 - 1)
                    style.withColor(ARGB.srgbLerp(blend, Output.FIRST_COLOR, Output.SECOND_COLOR))
                } else style
                sink.accept(index, animated, codePoint)
            }
        }
}
