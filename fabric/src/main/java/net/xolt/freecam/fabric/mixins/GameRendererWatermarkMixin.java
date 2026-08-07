package net.xolt.freecam.fabric.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.xolt.freecam.fabric.WatermarkRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererWatermarkMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    /**
     * Adds the mandatory mark after vanilla HUD extraction. In 1.21.10 F1
     * skips Gui.render entirely, but GuiRenderer still renders this state.
     */
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"
            )
    )
    private void freecamForYs$renderWatermark(
            DeltaTracker deltaTracker,
            boolean renderLevel,
            CallbackInfo ci
    ) {
        WatermarkRenderer.render(new GuiGraphics(minecraft, guiRenderState), deltaTracker);
    }
}
