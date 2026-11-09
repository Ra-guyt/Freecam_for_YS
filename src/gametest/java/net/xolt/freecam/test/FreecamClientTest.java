package net.xolt.freecam.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreecamPosition;

/** Runtime integration coverage; this source set is excluded from the release JAR. */
public class FreecamClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.runOnClient(mc -> {
                mc.gui.setScreen(null);
                Freecam.toggle();
                require(Freecam.isEnabled(), "Freecam must enable in a loaded world");
                var camera = Freecam.getFreeCamera();
                var pos = new FreecamPosition(camera);
                pos.y += 3;
                require(camera.tryApplyPosition(pos), "A nearby loaded air destination must be accepted");
                pos.x = mc.player.getX() + 51;
                require(!camera.tryApplyPosition(pos), "Moves beyond 50 blocks must be rejected");
                double safeX = camera.getX();
                Freecam.moveToPosition(pos);
                require(camera.getX() == safeX, "Public movement API must reject out-of-range targets");
                pos.x = mc.player.getX();
                pos.y = mc.player.getY() - 2;
                require(!camera.tryApplyPosition(pos), "Solid-block destinations must be rejected");
                require(!mc.gameMode.startDestroyBlock(mc.player.blockPosition().below(), net.minecraft.core.Direction.UP),
                        "Block destruction must be blocked");
                require(mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND)
                                == net.minecraft.world.InteractionResult.PASS,
                        "Item use must be blocked");
                ModConfig.INSTANCE.visual.fullBright = true;
                mc.gui.hud.toggle();
            });
            context.waitTicks(10);
            context.takeScreenshot("freecam-f1-watermark");
            context.runOnClient(mc -> {
                require(Freecam.isEnabled(), "Freecam must remain enabled during rendering");
                mc.player.setHealth(mc.player.getHealth() - 1);
            });
            context.waitTicks(2);
            context.runOnClient(mc -> {
                require(!Freecam.isEnabled(), "Damage must end Freecam");
                Freecam.toggle();
                require(Freecam.isEnabled(), "Reactivation must work after damage shutdown");
                Freecam.disableImmediately();
                require(!Freecam.isEnabled(), "Immediate safety shutdown must work");
                mc.gui.hud.toggle();
            });
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
