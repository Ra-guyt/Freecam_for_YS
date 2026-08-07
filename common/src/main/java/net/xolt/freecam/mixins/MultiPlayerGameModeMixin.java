package net.xolt.freecam.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onStopDestroyBlock(CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void onUseItem(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(
            Player player,
            Entity entity,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void onInteractAt(
            Player player,
            Entity entity,
            EntityHitResult hitResult,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (Freecam.isEnabled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Player player, Entity target, CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePickItemFromBlock", at = @At("HEAD"), cancellable = true)
    private void onPickItemFromBlock(BlockPos pos, boolean includeData, CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePickItemFromEntity", at = @At("HEAD"), cancellable = true)
    private void onPickItemFromEntity(Entity entity, boolean includeData, CallbackInfo ci) {
        if (Freecam.isEnabled()) {
            ci.cancel();
        }
    }
}
