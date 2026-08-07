package net.xolt.freecam.safety;

/**
 * Non-configurable safety limits for the YS-approved build.
 */
public final class CameraSafetyPolicy {

    public static final int MAX_CAMERA_DISTANCE_BLOCKS = 50;
    private static final double MAX_CAMERA_DISTANCE_SQUARED =
            MAX_CAMERA_DISTANCE_BLOCKS * MAX_CAMERA_DISTANCE_BLOCKS;

    private CameraSafetyPolicy() {
    }

    public static boolean isWithinRange(
            double playerX,
            double playerY,
            double playerZ,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        if (!areFinite(playerX, playerY, playerZ, cameraX, cameraY, cameraZ)) {
            return false;
        }

        double x = cameraX - playerX;
        double y = cameraY - playerY;
        double z = cameraZ - playerZ;
        return x * x + y * y + z * z <= MAX_CAMERA_DISTANCE_SQUARED;
    }

    private static boolean areFinite(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        return true;
    }
}
