package net.xolt.freecam.safety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraSafetyPolicyTest {

    @Test
    void acceptsTheExactBoundary() {
        assertTrue(CameraSafetyPolicy.isWithinRange(0, 0, 0, 50, 0, 0));
    }

    @Test
    void rejectsAValueJustOutsideTheBoundary() {
        assertFalse(CameraSafetyPolicy.isWithinRange(0, 0, 0, 50.0001, 0, 0));
    }

    @Test
    void usesThreeDimensionalDistance() {
        assertTrue(CameraSafetyPolicy.isWithinRange(10, 20, 30, 40, 50, 50));
        assertFalse(CameraSafetyPolicy.isWithinRange(10, 20, 30, 40, 60, 50));
    }

    @Test
    void rejectsNonFiniteCoordinates() {
        assertFalse(CameraSafetyPolicy.isWithinRange(0, 0, 0, Double.NaN, 0, 0));
        assertFalse(CameraSafetyPolicy.isWithinRange(0, 0, 0, Double.POSITIVE_INFINITY, 0, 0));
    }
}
