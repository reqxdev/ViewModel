package ViewModel.Mixins

import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import ViewModel.Config.Profiles

@Mixin(ItemInHandRenderer::class)
abstract class HandEquipMixin {
    /**
     * Keeps both hands fully raised: no equip dip, no attack-cooldown lowering.
     * Only the values read for rendering are overridden; the ticked height state is left alone.
     */
    @ModifyExpressionValue(
        method = ["submitHandsWithItems"],
        at = [
            At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;mainHandHeight:F"),
            At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;oMainHandHeight:F"),
            At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;offHandHeight:F"),
            At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;oOffHandHeight:F"),
        ],
        require = 4
    )
    private fun `viewmodel$lockEquipProgress`(height: Float): Float =
        if (Profiles.current.skipEquipAnimation) 1f else height

    @Inject(method = ["shouldInstantlyReplaceVisibleItem"], at = [At("HEAD")], cancellable = true)
    private fun `viewmodel$skipSwapAnimation`(current: ItemStack, expected: ItemStack, cir: CallbackInfoReturnable<Boolean>) {
        if (Profiles.current.skipEquipAnimation) cir.returnValue = true
    }
}
