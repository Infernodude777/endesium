package com.infernodude777.endesium;

import com.infernodude777.endesium.dragon.DragonFightController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dragon phase bands must stay identical to the client renderer's visual
 * stages: >75% phase 1, >45% phase 2, >20% phase 3, otherwise phase 4.
 */
class DragonPhaseThresholdTest {
	@Test
	void fullHealthIsPhaseOne() {
		assertEquals(1, DragonFightController.phaseFor(1.0F));
		assertEquals(1, DragonFightController.phaseFor(0.76F));
	}

	@Test
	void phaseTwoBand() {
		assertEquals(2, DragonFightController.phaseFor(0.75F));
		assertEquals(2, DragonFightController.phaseFor(0.46F));
	}

	@Test
	void phaseThreeBand() {
		assertEquals(3, DragonFightController.phaseFor(0.45F));
		assertEquals(3, DragonFightController.phaseFor(0.21F));
	}

	@Test
	void phaseFourAtTwentyPercentAndBelow() {
		assertEquals(4, DragonFightController.phaseFor(0.20F));
		assertEquals(4, DragonFightController.phaseFor(0.0F));
	}
}
