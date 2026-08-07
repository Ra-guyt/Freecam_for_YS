package net.xolt.freecam.mixins;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.xolt.freecam.util.FreeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;

    // When toggling freecam, update the camera's eye height instantly without any transition.
    @Inject(method = "setup", at = @At("HEAD"))
    public void onUpdate(BlockGetter area, Entity newFocusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (newFocusedEntity == null || this.entity == null || newFocusedEntity.equals(this.entity)) {
            return;
        }

        if (newFocusedEntity instanceof FreeCamera || this.entity instanceof FreeCamera) {
            this.eyeHeightOld = this.eyeHeight = newFocusedEntity.getEyeHeight();
        }
    }
}
