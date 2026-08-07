package net.xolt.freecam.fabric;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.xolt.freecam.Freecam;
import org.joml.Matrix3x2fStack;

/**
 * Mandatory identification mark for recordings made with the approved build.
 * Rendering is invoked from GameRendererWatermarkMixin after vanilla HUD
 * extraction, so F1 can hide the normal HUD without hiding this mark.
 */
public final class WatermarkRenderer {

    private static final String TEXT = "Freecam for YS";
    private static final ResourceLocation WATERMARK_SPRITE = ResourceLocation.fromNamespaceAndPath(
            Freecam.MOD_ID,
            "watermark"
    );
    private static final int LOGO_SIZE = 10;
    private static final int MARGIN = 3;
    private static final int GAP = 2;
    private static final int TEXT_Y_OFFSET = 3;
    // Keep the bitmap font on an exact 1:2 scale to avoid uneven glyph pixels.
    private static final float TEXT_SCALE = 0.5F;
    private static final float LOGO_ALPHA = 0.45F;
    private static final int TEXT_COLOR = 0x73FFFFFF;

    private WatermarkRenderer() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (!Freecam.isEnabled()) {
            return;
        }

        Font font = Freecam.MC.font;
        int scaledTextWidth = (int) Math.ceil(font.width(TEXT) * TEXT_SCALE);
        int totalWidth = LOGO_SIZE + GAP + scaledTextWidth;
        int x = graphics.guiWidth() - MARGIN - totalWidth;
        int y = graphics.guiHeight() - MARGIN - LOGO_SIZE;

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                WATERMARK_SPRITE,
                x,
                y,
                LOGO_SIZE,
                LOGO_SIZE,
                LOGO_ALPHA
        );

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x + LOGO_SIZE + GAP, y + TEXT_Y_OFFSET);
        pose.scale(TEXT_SCALE, TEXT_SCALE);
        graphics.drawString(font, TEXT, 0, 0, TEXT_COLOR, false);
        pose.popMatrix();
    }
}
