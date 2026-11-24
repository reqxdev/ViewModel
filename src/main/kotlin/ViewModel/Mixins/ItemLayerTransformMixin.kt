package ViewModel.Mixins

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import ViewModel.Config.Profiles
import ViewModel.Render.ItemRenderScope

@Mixin(targets = ["net.minecraft.client.renderer.item.ItemStackRenderState\$LayerRenderState"])
abstract class ItemLayerTransformMixin {
    @Inject(method = ["applyTransform"], at = [At("TAIL")])
    private fun `viewmodel$rotateAroundItem`(pose: PoseStack.Pose, ci: CallbackInfo) {
        if (!ItemRenderScope.active) return
        val settings = Profiles.current
        pose.rotate(Axis.XP.rotationDegrees(settings.rotationPitch))
        pose.rotate(Axis.YP.rotationDegrees(settings.rotationYaw))
        pose.rotate(Axis.ZP.rotationDegrees(settings.rotationRoll))
    }
}
