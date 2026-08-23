package com.infernodude777.endesium;

import com.infernodude777.endesium.world.ArenaGeometry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fracture-distance math shared by the arena builder and the dragon fight's
 * fissure damage (previously duplicated with drift risk between the two).
 */
class ArenaFractureDistanceTest {
	@Test
	void zeroDistanceOnSample() {
		List<double[]> points = List.of(new double[] { 10.0D, 20.0D, 2.0D });
		assertEquals(0.0D, ArenaGeometry.distanceToNearest(points, 10.0D, 20.0D), 1e-9);
	}

	@Test
	void pythagoreanDistance() {
		List<double[]> points = List.of(new double[] { 0.0D, 0.0D, 1.0D });
		assertEquals(5.0D, ArenaGeometry.distanceToNearest(points, 3.0D, -4.0D), 1e-9);
	}

	@Test
	void nearestOfSeveralWins() {
		List<double[]> points = List.of(
				new double[] { 100.0D, 100.0D, 1.0D },
				new double[] { 6.0D, 8.0D, 1.0D },
				new double[] { -50.0D, 3.0D, 1.0D });
		assertEquals(10.0D, ArenaGeometry.distanceToNearest(points, 0.0D, 0.0D), 1e-9);
	}

	@Test
	void deterministicSeedSampling() {
		double a = ArenaGeometry.distanceToFracture(8675309L, 12.5D, -40.25D);
		double b = ArenaGeometry.distanceToFracture(8675309L, 12.5D, -40.25D);
		assertTrue(a >= 0.0D);
		assertEquals(a, b, 0.0D);
	}
}
