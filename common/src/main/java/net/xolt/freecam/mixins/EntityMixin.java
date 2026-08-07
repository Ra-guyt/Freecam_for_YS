package net.xolt.freecam.mixins;

import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.freecam.Freecam.MC;

@Mixin(Entity.class)
@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class EntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double rotation, double pitch, CallbackInfo ci) {
        if (Freecam.isEnabled() && this.equals(MC.player) && Freecam.getFreeCamera() != null) {
            Freecam.getFreeCamera().turn(rotation, pitch);
            ci.cancel();
        }
    }
}
