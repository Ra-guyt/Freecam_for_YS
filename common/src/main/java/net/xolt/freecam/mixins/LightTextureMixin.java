package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.Lightmap;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Lightmap.class)
public class LightTextureMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/state/LightmapRenderState;brightness:F"))
    private float freecamForYs$brightness(float original) {
        return Freecam.isEnabled() && ModConfig.INSTANCE.visual.fullBright ? 16.0F : original;
    }
}
