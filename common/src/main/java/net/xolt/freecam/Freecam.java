package net.xolt.freecam;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import java.lang.Runnable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.safety.CameraSafetyPolicy;
import net.xolt.freecam.tripod.TripodRegistry;
import net.xolt.freecam.tripod.TripodSlot;
import net.xolt.freecam.util.FreeCamera;
import net.xolt.freecam.util.FreecamPosition;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class Freecam {

    public static final Minecraft MC = Minecraft.getInstance();
    public static final String MOD_ID = "freecam_for_ys";
    public static final int MAX_CAMERA_DISTANCE_BLOCKS = CameraSafetyPolicy.MAX_CAMERA_DISTANCE_BLOCKS;

    private static boolean freecamEnabled;
    private static boolean tripodEnabled;
    private static boolean disableNextTick;
    private static final TripodRegistry TRIPODS = new TripodRegistry();
    private static TripodSlot activeTripod = TripodSlot.NONE;
    private static FreeCamera freeCamera;
    private static FreecamPosition lastSafeCameraPosition;
    private static CameraType rememberedF5;
    private static int rejectionNoticeCooldown;

    @ApiStatus.Internal
    public static void preTick(Minecraft mc) {
        if (isEnabled() && (mc.player == null || mc.level == null || freeCamera == null)) {
            forceDisable();
            return;
        }

        if (isEnabled() && mc.player.isDeadOrDying()) {
            disableNextTick = true;
        }

        if ((disableNextTick || isRestrictedOnServer()) && isEnabled()) {
            forceDisable();
        }
        disableNextTick = false;

        if (isEnabled() && mc.player != null) {
            ClientInput input = new ClientInput();
            input.keyPresses = Input.EMPTY;
            mc.player.input = input;
        }
    }

    @ApiStatus.Internal
    public static void postTick(Minecraft mc) {
        if (rejectionNoticeCooldown > 0) {
            rejectionNoticeCooldown--;
        }
        if (isEnabled()) {
            enforceCameraSafety();
        }
        ModBindings.forEach(Runnable::run);
    }

    @ApiStatus.Internal
    public static void onDisconnect() {
        forceDisable();
        TRIPODS.clear();
    }

    @ApiStatus.Internal
    public static boolean activateTripodHandler() {
        boolean activated = false;
        for (KeyMapping combo : MC.options.keyHotbarSlots) {
            while (combo.consumeClick()) {
                toggleTripod(TripodSlot.ofKeyCode(combo.getDefaultKey().getValue()));
                activated = true;
            }
        }
        return activated;
    }

    @ApiStatus.Internal
    public static boolean resetTripodHandler() {
        boolean reset = false;
        for (KeyMapping key : MC.options.keyHotbarSlots) {
            while (key.consumeClick()) {
                resetCamera(TripodSlot.ofKeyCode(key.getDefaultKey().getValue()));
                reset = true;
            }
        }
        return reset;
    }

    @ApiStatus.AvailableSince("0.3.1")
    public static void toggle() {
        if (tripodEnabled) {
            toggleTripod(activeTripod);
            return;
        }

        if (freecamEnabled) {
            onDisableFreecam();
            freecamEnabled = false;
            onDisabled();
            return;
        }

        if (isRestrictedOnServer()) {
            notifyRestricted(ModConfig.INSTANCE.notification.notifyFreecam);
            return;
        }

        if (onEnableFreecam()) {
            freecamEnabled = true;
        }
    }

    private static void toggleTripod(TripodSlot tripod) {
        if (tripod == TripodSlot.NONE) {
            return;
        }

        if (tripodEnabled) {
            boolean closingActiveTripod = activeTripod == tripod;
            if (!closingActiveTripod && isRestrictedOnServer()) {
                notifyRestricted(ModConfig.INSTANCE.notification.notifyTripod);
                return;
            }
            onDisableTripod();
            tripodEnabled = false;
            if (!closingActiveTripod) {
                tripodEnabled = onEnableTripod(tripod);
            }
        } else {
            if (freecamEnabled) {
                if (isRestrictedOnServer()) {
                    notifyRestricted(ModConfig.INSTANCE.notification.notifyTripod);
                    return;
                }
                onDisableFreecam();
                freecamEnabled = false;
            }
            if (isRestrictedOnServer()) {
                notifyRestricted(ModConfig.INSTANCE.notification.notifyTripod);
                return;
            }
            tripodEnabled = onEnableTripod(tripod);
        }

        if (!isEnabled()) {
            onDisabled();
        }
    }

    private static boolean onEnableTripod(TripodSlot tripod) {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        FreecamPosition position = TRIPODS.get(tripod);
        if (position != null) {
            MoveRejection rejection = validatePosition(position);
            if (rejection != MoveRejection.NONE) {
                notifyMoveRejected(rejection);
                return false;
            }
        }

        onEnable();
        freeCamera = new FreeCamera(-420 - tripod.ordinal());

        boolean moved = position == null ? moveToPlayerInternal() : tryMoveToPosition(position, true);
        if (!moved) {
            freeCamera = null;
            abortEnable();
            return false;
        }

        freeCamera.spawn();
        MC.setCameraEntity(freeCamera);
        activeTripod = tripod;

        if (ModConfig.INSTANCE.notification.notifyTripod) {
            MC.gui.hud.setOverlayMessage(Component.translatable("msg.freecam_for_ys.openTripod", tripod), true);
        }
        return true;
    }

    private static void onDisableTripod() {
        TripodSlot closingTripod = activeTripod;
        if (freeCamera != null && closingTripod != TripodSlot.NONE) {
            TRIPODS.put(closingTripod, new FreecamPosition(freeCamera));
        }
        onDisable();

        if (MC.player != null && ModConfig.INSTANCE.notification.notifyTripod) {
            MC.gui.hud.setOverlayMessage(Component.translatable("msg.freecam_for_ys.closeTripod", closingTripod), true);
        }
        activeTripod = TripodSlot.NONE;
    }

    private static boolean onEnableFreecam() {
        if (MC.player == null || MC.level == null) {
            return false;
        }

        onEnable();
        freeCamera = new FreeCamera(-420);
        if (!moveToPlayerInternal()) {
            freeCamera = null;
            abortEnable();
            return false;
        }

        freeCamera.spawn();
        MC.setCameraEntity(freeCamera);

        if (ModConfig.INSTANCE.notification.notifyFreecam) {
            MC.gui.hud.setOverlayMessage(
                    Component.translatable("msg.freecam_for_ys.enable", MAX_CAMERA_DISTANCE_BLOCKS),
                    true
            );
        }
        return true;
    }

    private static void onDisableFreecam() {
        onDisable();
        if (MC.player != null && ModConfig.INSTANCE.notification.notifyFreecam) {
            MC.gui.hud.setOverlayMessage(Component.translatable("msg.freecam_for_ys.disable"), true);
        }
    }

    private static void onEnable() {
        MC.smartCull = false;
        if (rememberedF5 == null) {
            rememberedF5 = MC.options.getCameraType();
        }
        if (MC.gameRenderer.mainCamera().isDetached()) {
            MC.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    private static void abortEnable() {
        MC.smartCull = true;
        if (MC.player != null) {
            MC.setCameraEntity(MC.player);
            MC.player.input = new KeyboardInput(MC.options);
        }
        lastSafeCameraPosition = null;
    }

    private static void onDisable() {
        MC.smartCull = true;
        if (MC.player != null) {
            MC.setCameraEntity(MC.player);
        }
        if (freeCamera != null) {
            freeCamera.despawn();
            freeCamera.input = new ClientInput();
            freeCamera = null;
        }
        if (MC.player != null) {
            MC.player.input = new KeyboardInput(MC.options);
        }
        lastSafeCameraPosition = null;
    }

    private static void onDisabled() {
        if (rememberedF5 != null) {
            MC.options.setCameraType(rememberedF5);
            rememberedF5 = null;
        }
    }

    private static void forceDisable() {
        if (isEnabled() || freeCamera != null) {
            onDisable();
        }
        freecamEnabled = false;
        tripodEnabled = false;
        activeTripod = TripodSlot.NONE;
        disableNextTick = false;
        onDisabled();
    }

    private static void resetCamera(TripodSlot tripod) {
        if (tripod == TripodSlot.NONE) {
            return;
        }
        if (tripodEnabled && activeTripod == tripod && freeCamera != null) {
            moveToPlayer();
        } else {
            TRIPODS.put(tripod, null);
        }

        if (MC.player != null && ModConfig.INSTANCE.notification.notifyTripod) {
            MC.gui.hud.setOverlayMessage(Component.translatable("msg.freecam_for_ys.tripodReset", tripod), true);
        }
    }

    @ApiStatus.Experimental
    @ApiStatus.AvailableSince("1.2.3")
    public static void moveToEntity(@Nullable Entity entity) {
        if (freeCamera == null) {
            return;
        }
        if (entity == null) {
            moveToPlayer();
            return;
        }
        if (entity.level() != MC.level) {
            notifyMoveRejected(MoveRejection.UNAVAILABLE);
            return;
        }
        tryMoveToPosition(new FreecamPosition(entity), true);
    }

    @ApiStatus.Experimental
    @ApiStatus.AvailableSince("1.2.3")
    public static void moveToPosition(@Nullable FreecamPosition position) {
        if (freeCamera == null) {
            return;
        }
        if (position == null) {
            moveToPlayer();
            return;
        }
        tryMoveToPosition(position, true);
    }

    @ApiStatus.Experimental
    @ApiStatus.AvailableSince("1.2.3")
    public static void moveToPlayer() {
        if (freeCamera != null) {
            moveToPlayerInternal();
        }
    }

    private static boolean moveToPlayerInternal() {
        if (freeCamera == null || MC.player == null) {
            return false;
        }

        if (!freeCamera.tryApplyPosition(new FreecamPosition(MC.player))) {
            return false;
        }
        freeCamera.applyPerspective(ModConfig.INSTANCE.visual.perspective, true);
        lastSafeCameraPosition = new FreecamPosition(freeCamera);
        return true;
    }

    private static boolean tryMoveToPosition(FreecamPosition position, boolean notify) {
        MoveRejection rejection = validatePosition(position);
        if (rejection == MoveRejection.NONE && !freeCamera.tryApplyPosition(position)) {
            rejection = MoveRejection.COLLISION;
        }

        if (rejection != MoveRejection.NONE) {
            if (notify) {
                notifyMoveRejected(rejection);
            }
            return false;
        }

        lastSafeCameraPosition = new FreecamPosition(freeCamera);
        return true;
    }

    private static MoveRejection validatePosition(FreecamPosition position) {
        if (position == null || MC.player == null || MC.level == null) {
            return MoveRejection.UNAVAILABLE;
        }
        if (!CameraSafetyPolicy.isWithinRange(
                MC.player.getX(), MC.player.getY(), MC.player.getZ(),
                position.x, position.y, position.z
        )) {
            return MoveRejection.OUT_OF_RANGE;
        }

        var chunk = position.getChunkPos();
        if (!MC.level.getChunkSource().hasChunk(chunk.x(), chunk.z())) {
            return MoveRejection.UNLOADED;
        }
        return MoveRejection.NONE;
    }

    private static void enforceCameraSafety() {
        if (freeCamera == null || MC.player == null || MC.level == null) {
            forceDisable();
            return;
        }

        FreecamPosition current = new FreecamPosition(freeCamera);
        MoveRejection rejection = validatePosition(current);
        if (rejection == MoveRejection.NONE && freeCamera.isCurrentPositionCollisionFree()) {
            lastSafeCameraPosition = current;
            return;
        }
        if (rejection == MoveRejection.NONE) {
            rejection = MoveRejection.COLLISION;
        }

        boolean restored = lastSafeCameraPosition != null
                && validatePosition(lastSafeCameraPosition) == MoveRejection.NONE
                && freeCamera.tryApplyPosition(lastSafeCameraPosition);
        if (!restored) {
            moveToPlayerInternal();
        }
        notifyMoveRejected(rejection);
    }

    private static void notifyMoveRejected(MoveRejection rejection) {
        if (MC.player == null || rejectionNoticeCooldown > 0) {
            return;
        }

        Component message = switch (rejection) {
            case OUT_OF_RANGE -> Component.translatable(
                    "msg.freecam_for_ys.moveRejected.outOfRange",
                    MAX_CAMERA_DISTANCE_BLOCKS
            );
            case UNLOADED -> Component.translatable("msg.freecam_for_ys.moveRejected.unloaded");
            case COLLISION -> Component.translatable("msg.freecam_for_ys.moveRejected.collision");
            case UNAVAILABLE -> Component.translatable("msg.freecam_for_ys.moveRejected.unavailable");
            case NONE -> null;
        };
        if (message != null) {
            MC.gui.hud.setOverlayMessage(message, true);
            rejectionNoticeCooldown = 40;
        }
    }

    private static void notifyRestricted(boolean enabled) {
        ServerData server = MC.getCurrentServer();
        if (enabled && MC.player != null && server != null) {
            MC.gui.hud.setOverlayMessage(
                    Component.translatable("msg.freecam_for_ys.restrictedByConfig", server.ip),
                    true
            );
        }
    }

    @ApiStatus.AvailableSince("0.4.0")
    public static FreeCamera getFreeCamera() {
        return freeCamera;
    }

    @ApiStatus.AvailableSince("1.2.3")
    public static void disableNextTick() {
        disableNextTick = true;
    }

    @ApiStatus.Internal
    public static void disableImmediately() {
        forceDisable();
    }

    @ApiStatus.AvailableSince("0.2.2")
    public static boolean isEnabled() {
        return freecamEnabled || tripodEnabled;
    }

    @ApiStatus.Experimental
    @ApiStatus.AvailableSince("1.2.4")
    public static boolean isRestrictedOnServer() {
        if (ModConfig.INSTANCE == null) {
            return false;
        }

        ServerData server = MC.getCurrentServer();
        ModConfig.ServerRestriction mode = ModConfig.INSTANCE.servers.mode;
        if (mode == ModConfig.ServerRestriction.NONE || server == null || MC.hasSingleplayerServer()) {
            return false;
        }

        String ip = server.ip.trim().toLowerCase();
        return switch (mode) {
            case WHITELIST -> ModConfig.INSTANCE.servers.whitelist.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .noneMatch(ip::equals);
            case BLACKLIST -> ModConfig.INSTANCE.servers.blacklist.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .anyMatch(ip::equals);
            default -> throw new IllegalStateException("Unexpected server restriction mode: " + mode);
        };
    }

    private enum MoveRejection {
        NONE,
        OUT_OF_RANGE,
        UNLOADED,
        COLLISION,
        UNAVAILABLE
    }
}
