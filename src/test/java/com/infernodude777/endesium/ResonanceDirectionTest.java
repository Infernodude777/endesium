package com.infernodude777.endesium;

import com.infernodude777.endesium.resonance.ResonanceManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Direction-bucket contract: eight buckets, east=0, increasing clockwise
 * (south-east, south, ...), matching CARDINALS[0..7]. The historical bug this
 * guards against is a PI offset that rotated every Lens reading by 180°.
 */
class ResonanceDirectionTest {
	@Test
	void eastIsBucketZero() {
		assertEquals(0, ResonanceManager.bucketFor(10.0D, 0.0D));
	}

	@Test
	void southEastIsBucketOne() {
		assertEquals(1, ResonanceManager.bucketFor(5.0D, 5.0D));
	}

	@Test
	void southIsBucketTwo() {
		assertEquals(2, ResonanceManager.bucketFor(0.0D, 10.0D));
	}

	@Test
	void westIsOppositeOfEast() {
		assertEquals(4, ResonanceManager.bucketFor(-10.0D, 0.0D));
	}

	@Test
	void northWrapsToBucketFiveThroughSeven() {
		int b = ResonanceManager.bucketFor(0.0D, -10.0D);
		org.junit.jupiter.api.Assertions.assertTrue(b >= 5 && b <= 7,
				"north should land in the wrapped upper half, got " + b);
	}

	@Test
	void cardinalsMatchBuckets() {
		assertEquals("east", ResonanceManager.cardinal(0));
		assertEquals("south", ResonanceManager.cardinal(2));
		assertEquals("north", ResonanceManager.cardinal(6));
		assertEquals("the void", ResonanceManager.cardinal(-1));
		assertEquals("the void", ResonanceManager.cardinal(99));
	}
}
