package com.infernodude777.endesium;

import com.infernodude777.endesium.item.VoidAnchorItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Anchor expiry contract: pre-timestamp anchors (boundAt <= 0) are expired,
 * fresh anchors live, and anything past the 1200-tick lifetime fades.
 */
class VoidAnchorExpiryTest {
	@Test
	void legacyAnchorsWithoutTimestampExpire() {
		assertTrue(VoidAnchorItem.isExpired(0L, 5000L));
		assertTrue(VoidAnchorItem.isExpired(-1L, 100L));
	}

	@Test
	void freshAnchorIsAlive() {
		assertFalse(VoidAnchorItem.isExpired(1000L, 1500L));
	}

	@Test
	void lifetimeBoundaryExpires() {
		long bound = 10_000L;
		assertFalse(VoidAnchorItem.isExpired(bound, bound + 1200L));
		assertTrue(VoidAnchorItem.isExpired(bound, bound + 1201L));
	}
}
