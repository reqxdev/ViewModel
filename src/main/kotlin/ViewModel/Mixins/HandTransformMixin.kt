package ViewModel.Mixins

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState
import net.minecraft.client.renderer.state.level.PlayerRenderState
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import ViewModel.Config.Profiles
import ViewModel.Render.ItemRenderScope

@Mixin(FirstPersonHandsAndItemsRenderer::class)
abstract class HandTransformMixin {
    @Inject(
        method = ["submitArmWithItem"],
        at = [At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER)]
    )
    private fun `viewmodel$position`(
        playerState: PlayerRenderState, state: FirstPersonHandsAndItemsRenderState, partialTicks: Float, xRot: Float,
        hand: InteractionHand, attack: Float, itemStack: ItemStack, inverseArmHeight: Float,
        poseStack: PoseStack, collector: SubmitNodeCollector, lightCoords: Int, ci: CallbackInfo
    ) {
        // Empty hands go through the same scope; offsetting them would make the bare arm jump.
        if (itemStack.isEmpty) return
        val settings = Profiles.current
        val handSign = if (hand == InteractionHand.MAIN_HAND) 1f else -1f
        poseStack.translate(settings.positionX / 100f * handSign, settings.positionY / 100f, settings.positionZ / 100f)
    }

    /** Scales the held item and marks the submit so [ItemLayerTransformMixin] rotates it around its own centre. */
    @WrapOperation(
        method = ["submitArmWithItem"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )]
    )
    private fun `viewmodel$scaleAndScope`(
        renderState: ItemStackRenderState, poseStack: PoseStack, collector: SubmitNodeCollector,
        light: Int, overlay: Int, outline: Int, original: Operation<Void>
    ) {
        val scale = Profiles.current.size
        if (scale != 1f) poseStack.scale(scale, scale, scale)
        ItemRenderScope.depth++
        try {
            original.call(renderState, poseStack, collector, light, overlay, outline)
        } finally {
            ItemRenderScope.depth--
        }
    }
}
