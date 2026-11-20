package ViewModel.Mixins

import net.minecraft.client.Minecraft
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.entity.LivingEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import ViewModel.Config.Profiles
import kotlin.math.roundToInt

@Mixin(LivingEntity::class)
abstract class SwingSpeedMixin {
    @Inject(method = ["getCurrentSwingDuration"], at = [At("RETURN")], cancellable = true)
    private fun `viewmodel$swingSpeed`(cir: CallbackInfoReturnable<Int>) {
        val self = this as Any as LivingEntity
        if (self !== Minecraft.getInstance().player) return
        val settings = Profiles.current
        var duration = cir.returnValue
        if (settings.noHaste && MobEffectUtil.hasDigSpeed(self)) {
            duration += 1 + MobEffectUtil.getDigSpeedAmplification(self)
        }
        val speed = settings.swingMultiplier
        if (speed != 1f && speed > 0f) duration = (duration / speed).roundToInt().coerceAtLeast(1)
        cir.returnValue = duration
    }
}
