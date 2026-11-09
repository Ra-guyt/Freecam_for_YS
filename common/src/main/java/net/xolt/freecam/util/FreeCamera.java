package net.xolt.freecam.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.ServerLinks;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.state.BlockState;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.safety.CameraSafetyPolicy;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.UUID;

import static net.xolt.freecam.Freecam.MC;

@ApiStatus.Internal
@ApiStatus.AvailableSince("0.4.0")
public class FreeCamera extends LocalPlayer {

    private static final ClientPacketListener NETWORK_HANDLER = new ClientPacketListener(
            MC,
            new net.minecraft.network.Connection(PacketFlow.CLIENTBOUND),
            new CommonListenerCookie(
                    // levelLoadTracker
                    new LevelLoadTracker(),
                    // localGameProfile
                    new GameProfile(UUID.randomUUID(), "FreeCamera"),
                    // worldSessionTelemetryManager
                    MC.getTelemetryManager().createWorldSessionManager(false, null, null, null),
                    // receivedRegistries
                    MC.player.registryAccess().freeze(),
                    // enabledFeatures
                    FeatureFlagSet.of(),
                    // serverBrand
                    null,
                    // serverData
                    null,
                    // postDisconnectScreen
                    null,
                    // serverCookies
                    Collections.emptyMap(),
                    // chatState
                    null,
                    // customReportDetails
                    Collections.emptyMap(),
                    // serverLinks
                    ServerLinks.EMPTY,
                    // seenPlayers
                    Collections.emptyMap(),
                    // seenInsecureChatWarning
                    false)) {
        @Override
        public void send(Packet<?> packet) {
        }
    };

    public FreeCamera(int id) {
        super(MC, MC.level, NETWORK_HANDLER, MC.player.getStats(), MC.player.getRecipeBook(), Input.EMPTY, false, MC.player.chatAbilities());

        setId(id);
        setPose(Pose.SWIMMING);
        getAbilities().flying = true;
        input = new KeyboardInput(MC.options);
    }

    @Override
    public void copyPosition(Entity entity) {
        tryApplyPosition(new FreecamPosition(entity));
    }

    private void applyPositionUnchecked(FreecamPosition position) {
        snapTo(position.x, position.y, position.z, position.yaw, position.pitch);
        xBob = getXRot();
        yBob = getYRot();
        xBobO = xBob; // Prevents camera from rotating upon entering freecam.
        yBobO = yBob;
    }

    /**
     * Applies a direct camera move only when it remains inside the fixed YS
     * boundary, targets a loaded chunk, and does not intersect block collision.
     */
    public boolean tryApplyPosition(FreecamPosition position) {
        if (!isPositionPolicyAllowed(position)) {
            return false;
        }

        FreecamPosition previous = new FreecamPosition(this);
        applyPositionUnchecked(position);
        if (!isCurrentPositionCollisionFree()) {
            applyPositionUnchecked(previous);
            return false;
        }
        return true;
    }

    // Mutate the position and rotation based on perspective
    // If checkCollision is true, move as far as possible without colliding
    public void applyPerspective(ModConfig.Perspective perspective, boolean checkCollision) {
        FreecamPosition position = new FreecamPosition(this);

        switch (perspective) {
            case INSIDE:
                // No-op
                break;
            case FIRST_PERSON:
                // Move just in front of the player's eyes
                moveForwardUntilCollision(position, 0.4, checkCollision);
                break;
            case THIRD_PERSON_MIRROR:
                // Invert the rotation and fallthrough into the THIRD_PERSON case
                position.mirrorRotation();
            case THIRD_PERSON:
                // Move back as per F5 mode
                moveForwardUntilCollision(position, -4.0, checkCollision);
                break;
        }
    }

    // Move FreeCamera forward using FreecamPosition.moveForward.
    // If checkCollision is true, stop moving forward before hitting a collision.
    // Return true if successfully able to move.
    private boolean moveForwardUntilCollision(FreecamPosition position, double distance, boolean checkCollision) {
        // This fork intentionally ignores requests to bypass collision.
        return moveForwardUntilCollision(position, distance);
    }

    // Same as above, but always check collision.
    private boolean moveForwardUntilCollision(FreecamPosition position, double maxDistance) {
        boolean negative = maxDistance < 0;
        maxDistance = negative ? -1 * maxDistance : maxDistance;
        double increment = 0.1;

        // Move forward by increment until we reach maxDistance or hit a collision
        for (double distance = 0.0; distance < maxDistance; distance += increment) {
            FreecamPosition oldPosition = new FreecamPosition(this);

            position.moveForward(negative ? -1 * increment : increment);
            if (!isPositionPolicyAllowed(position)) {
                return distance > 0;
            }
            applyPositionUnchecked(position);

            if (!isCurrentPositionCollisionFree()) {
                // Revert to last non-colliding position and return whether we were unable to move at all
                applyPositionUnchecked(oldPosition);
                return distance > 0;
            }
        }

        return true;
    }

    public boolean isCurrentPositionCollisionFree() {
        return wouldNotSuffocateAtTargetPose(getPose());
    }

    private boolean isPositionPolicyAllowed(FreecamPosition position) {
        if (position == null || MC.player == null || MC.level == null
                || !Float.isFinite(position.yaw) || !Float.isFinite(position.pitch)) {
            return false;
        }
        if (!CameraSafetyPolicy.isWithinRange(
                MC.player.getX(), MC.player.getY(), MC.player.getZ(),
                position.x, position.y, position.z
        )) {
            return false;
        }
        var chunk = position.getChunkPos();
        return MC.level.getChunkSource().hasChunk(chunk.x(), chunk.z());
    }

    public void spawn() {
        ((ClientLevel) level()).addEntity(this);
    }

    public void despawn() {
        ((ClientLevel) level()).removeEntity(getId(), RemovalReason.DISCARDED);
    }

    // Prevents a local fall sound when the collision-enabled camera touches ground.
    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    // Needed for hand swings to be shown in freecam since the player is replaced by FreeCamera in HeldItemRenderer.renderItem()
    @Override
    public float getAttackAnim(float tickDelta) {
        return MC.player.getAttackAnim(tickDelta);
    }

    // Needed for item use animations to be shown in freecam since the player is replaced by FreeCamera in HeldItemRenderer.renderItem()
    @Override
    public int getUseItemRemainingTicks() {
        return MC.player.getUseItemRemainingTicks();
    }

    // Also needed for item use animations to be shown in freecam.
    @Override
    public boolean isUsingItem() {
        return MC.player.isUsingItem();
    }

    // Prevents slow down from ladders/vines.
    @Override
    public boolean onClimbable() {
        return false;
    }

    // Prevents slow down from water.
    @Override
    public boolean isInWater() {
        return false;
    }

    // Makes night vision apply to FreeCamera when Iris is enabled.
    @Override
    public MobEffectInstance getEffect(Holder<MobEffect> holder) {
        return MC.player.getEffect(holder);
    }

    // Ensures that the FreeCamera is always in the swimming pose.
    @Override
    public void setPose(Pose pose) {
        super.setPose(Pose.SWIMMING);
    }

    // Prevents slow down due to being in swimming pose. (Fixes being unable to sprint)
    @Override
    public boolean isMovingSlowly() {
        return false;
    }

    // Prevents water submersion sounds from playing.
    @Override
    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    // Prevents water submersion sounds from playing.
    @Override
    protected void doWaterSplashEffect() {}

    @Override
    public void aiStep() {
        if (ModConfig.INSTANCE.movement.flightMode.equals(ModConfig.FlightMode.DEFAULT)) {
            getAbilities().setFlyingSpeed(0);
            Motion.doMotion(this, ModConfig.INSTANCE.movement.horizontalSpeed, ModConfig.INSTANCE.movement.verticalSpeed);
        } else {
            getAbilities().setFlyingSpeed((float) ModConfig.INSTANCE.movement.verticalSpeed / 10);
        }
        super.aiStep();
        getAbilities().flying = true;
        setOnGround(false);
    }
}
