package ViewModel.Mixins

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import ViewModel.Config.Profiles

@Mixin(FirstPersonHandsAndItemsRenderer::class)
abstract class HandSwingMixin {
    @Shadow protected abstract fun applyItemArmAttackTransform(poseStack: PoseStack, arm: HumanoidArm, attackValue: Float)

    @Inject(method = ["swingArm"], at = [At("HEAD")], cancellable = true)
    private fun `viewmodel$disableSwing`(animation: Float, poseStack: PoseStack, invert: Int, arm: HumanoidArm, ci: CallbackInfo) {
        if (!Profiles.current.noSwing) return
        ci.cancel()
        applyItemArmAttackTransform(poseStack, arm, animation)
    }

    @Redirect(method = ["swingArm"], at = [At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")])
    private fun `viewmodel$scaleTranslation`(poseStack: PoseStack, x: Float, y: Float, z: Float) {
        val settings = Profiles.current
        val scale = if (settings.scaleSwing) settings.size else 1f
        poseStack.translate(x * scale, y * scale, z * scale)
    }

    @Inject(method = ["applyItemArmAttackTransform"], at = [At("HEAD")], cancellable = true)
    private fun `viewmodel$scaleRotation`(poseStack: PoseStack, arm: HumanoidArm, attackValue: Float, ci: CallbackInfo) {
        val settings = Profiles.current
        if (!settings.scaleSwing) return
        val invert = if (arm == HumanoidArm.RIGHT) 1 else -1
        val scale = settings.size
        val squared = Mth.sin(attackValue * attackValue * Math.PI)
        val rooted = Mth.sin(Mth.sqrt(attackValue) * Math.PI)
        poseStack.rotateDegrees(Axis.YP, invert * (45f - squared * 20f * scale))
        poseStack.rotateDegrees(Axis.ZP, invert * rooted * -20f * scale)
        poseStack.rotateDegrees(Axis.XP, rooted * -80f * scale)
        poseStack.rotateDegrees(Axis.YP, invert * -45f)
        ci.cancel()
    }
}
