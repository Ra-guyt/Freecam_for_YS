package net.xolt.freecam.mixins;

import net.minecraft.client.Minecraft;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.xolt.freecam.config.ModBindings.KEY_TOGGLE;
import static net.xolt.freecam.config.ModBindings.KEY_TRIPOD_RESET;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onStartUseItem(CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void onPickBlock(CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    // Prevent hotbar keys from changing the selected slot while an F4+number
    // tripod combination is being handled.
    @Inject(
            method = "handleKeybinds",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2),
            cancellable = true
    )
    private void onHandleKeybinds(CallbackInfo ci) {
        if (KEY_TOGGLE.get().isDown() || KEY_TRIPOD_RESET.get().isDown()) {
            ci.cancel();
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        Freecam.onDisconnect();
    }
}
