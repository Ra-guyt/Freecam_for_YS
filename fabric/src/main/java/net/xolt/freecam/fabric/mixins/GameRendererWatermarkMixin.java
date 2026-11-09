package net.xolt.freecam.fabric.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.xolt.freecam.fabric.WatermarkRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the mark to every GUI frame, outside the HUD's F1 visibility check. */
@Mixin(Gui.class)
public abstract class GameRendererWatermarkMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private GuiRenderState guiRenderState;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void freecamForYs$renderWatermark(DeltaTracker deltaTracker, boolean renderLevel,
                                            boolean renderDebug, CallbackInfo ci) {
        WatermarkRenderer.render(new GuiGraphicsExtractor(minecraft, guiRenderState, 0, 0), deltaTracker);
    }
}
