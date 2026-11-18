package ViewModel.Mixins

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LivingEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import ViewModel.Config.Profiles
import kotlin.math.roundToInt

@Mixin(LivingEntity::class)
abstract class SwingSpeedMixin {
    /** Scales the local player's swing length (in ticks) by the profile's Speed; other entities are untouched. */
    @Inject(method = ["getCurrentSwingDuration"], at = [At("RETURN")], cancellable = true)
    private fun `viewmodel$swingSpeed`(cir: CallbackInfoReturnable<Int>) {
        if ((this as Any) !== Minecraft.getInstance().player) return
        val speed = Profiles.current.swingSpeed
        if (speed == 1f || speed <= 0f) return
        cir.returnValue = (cir.returnValue / speed).roundToInt().coerceAtLeast(1)
    }
}
