package ViewModel.Mixins

import net.minecraft.client.player.FirstPersonHandsAndItems
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import ViewModel.Config.Profiles

@Mixin(FirstPersonHandsAndItems::class)
abstract class HandEquipMixin {
    /** Keeps both hands fully raised: no equip dip, no attack-cooldown lowering. */
    @Inject(method = ["extractRenderState"], at = [At("TAIL")])
    private fun `viewmodel$lockEquipProgress`(
        player: LocalPlayer, partialTicks: Float, state: FirstPersonHandsAndItemsRenderState, ci: CallbackInfo
    ) {
        if (!Profiles.current.skipEquipAnimation) return
        state.mainHandHeight = 1f
        state.oldMainHandHeight = 1f
        state.offHandHeight = 1f
        state.oldOffHandHeight = 1f
    }

    @Inject(method = ["shouldInstantlyReplaceVisibleItem"], at = [At("HEAD")], cancellable = true)
    private fun `viewmodel$skipSwapAnimation`(
        current: ItemStack, expected: ItemStack, player: LocalPlayer, cir: CallbackInfoReturnable<Boolean>
    ) {
        if (Profiles.current.skipEquipAnimation) cir.returnValue = true
    }
}
