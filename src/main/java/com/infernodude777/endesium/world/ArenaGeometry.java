package com.infernodude777.endesium.world;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic macro and meso geography for the central End island.
 *
 * <p>The island is deliberately authored as one connected geological mass:
 * an irregular broad core with six seed-varied outcrop/ridge arms, not a disk
 * made from independent plates. Height follows those same connected forms,
 * then adds basins, cliffs, meso relief, and a small number of fractures.</p>
 */
public final class ArenaGeometry {
	/** The arena is architecturally anchored at the world origin. */
	public static final double ARENA_CENTER_X = 0.0D;
	public static final double ARENA_CENTER_Z = 0.0D;
	/** Typical surface height at the arena plateau, for origin-anchored FX. */
	public static final double ARENA_SURFACE_Y = 66.0D;
	/** Safe zone for the catastrophic Resonance attack. */
	public static final double SAFE_ZONE_RADIUS = 8.0D;
	/** Small, incomplete ancient marking around the portal. */
	public static final double RING_RADIUS = 18.0D;
	/** Solid underside for newly extended End Stone shelves. */
	public static final int SHELF_BOTTOM = 40;

	/** Vanilla 1.21.1 pillar bases; their gameplay geometry remains untouched. */
	private static final int[][] PILLAR_POSITIONS = {
			{42, 0}, {0, 42}, {-42, 0}, {0, -42},
			{30, 30}, {-30, 30}, {30, -30}, {-30, -30},
			{15, 30}, {30, 15}, {-15, 30}, {30, -15},
			{15, -30}, {-15, -30}, {-30, 15}, {-30, -15},
			{-42, -42}, {42, -42}, {-42, 42}, {42, 42},
	};

	private ArenaGeometry() {
	}

	// ---------------------------------------------------------------------
	// Deterministic noise
	// ---------------------------------------------------------------------

	private static long mix(long seed, int x, int z) {
		long h = seed * 0x9E3779B97F4A7C15L;
		h ^= (long) x * 0x85EBCA6BL;
		h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
		h ^= h >>> 29;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 32;
		return h;
	}

	/** Hashed value noise in [0, 1). */
	public static double valueNoise(long seed, int x, int z) {
		long h = mix(seed, x, z);
		return (h & 0xFFFFFFL) / 16777215.0D;
	}

	/** Smoothly interpolated value noise in [0, 1). */
	public static double smoothNoise(long seed, double x, double z) {
		int xi = (int) Math.floor(x);
		int zi = (int) Math.floor(z);
		double fx = x - xi;
		double fz = z - zi;
		double sx = fx * fx * (3.0D - 2.0D * fx);
		double sz = fz * fz * (3.0D - 2.0D * fz);
		double a = valueNoise(seed, xi, zi);
		double b = valueNoise(seed, xi + 1, zi);
		double c = valueNoise(seed, xi, zi + 1);
		double d = valueNoise(seed, xi + 1, zi + 1);
		return a + (b - a) * sx + (c - a) * sz + (a - b - c + d) * sx * sz;
	}

	/** Fractal Brownian motion in [0, 1). */
	public static double fbm(long seed, double x, double z) {
		double sum = 0.0D;
		double amplitude = 1.0D;
		double total = 0.0D;
		double fx = x;
		double fz = z;
		for (int i = 0; i < 4; i++) {
			sum += smoothNoise(seed + i * 1013L, fx, fz) * amplitude;
			total += amplitude;
			amplitude *= 0.5D;
			fx *= 2.0D;
			fz *= 2.0D;
		}
		return sum / total;
	}

	private static double clamp01(double value) {
		return Math.clamp(value, 0.0D, 1.0D);
	}

	private static double smoothstep(double edge0, double edge1, double value) {
		double t = clamp01((value - edge0) / (edge1 - edge0));
		return t * t * (3.0D - 2.0D * t);
	}

	// ---------------------------------------------------------------------
	// Connected island mask
	// ---------------------------------------------------------------------

	private static double[] landmarkEnd(long seed, int index) {
		// Spread the major regions around the core without making identical
		// spokes: large angular jitter breaks any rotational symmetry while the
		// six broad landforms remain connected and compositionally balanced.
		double angle = index * (Math.PI * 2.0D / 6.0D)
				+ (valueNoise(seed, 210 + index * 19, 17) - 0.5D) * 0.85D;
		double length = 76.0D + valueNoise(seed, 410 + index * 23, 29) * 72.0D;
		return new double[] { Math.cos(angle) * length, Math.sin(angle) * length };
	}

	private static boolean isCoastBay(long seed, double x, double z) {
		for (int i = 0; i < 4; i++) {
			double angle = (i + 0.35D) * (Math.PI * 2.0D / 4.0D)
					+ (valueNoise(seed, 1210 + i * 17, 71) - 0.5D) * 0.42D;
			double inner = 74.0D + valueNoise(seed, 1270 + i * 13, 73) * 28.0D;
			double outer = inner + 48.0D;
			double startX = Math.cos(angle) * inner;
			double startZ = Math.sin(angle) * inner;
			double endX = Math.cos(angle) * outer;
			double endZ = Math.sin(angle) * outer;
			double width = 13.0D + valueNoise(seed, 1330 + i * 11, 79) * 13.0D;
			if (segmentDistanceBetween(x, z, startX, startZ, endX, endZ) < width) return true;
			// A small sideways bite makes each bay asymmetrical rather than a
			// straight procedural notch.
			double sideX = -Math.sin(angle) * width * 0.75D;
			double sideZ = Math.cos(angle) * width * 0.75D;
			if (segmentDistanceBetween(x, z, startX + sideX, startZ + sideZ,
					endX + sideX, endZ + sideZ) < width * 0.55D) return true;
		}
		return false;
	}

	private static double segmentDistance(double x, double z, double endX, double endZ) {
		return segmentDistanceBetween(x, z, 0.0D, 0.0D, endX, endZ);
	}

	private static double segmentDistanceBetween(double x, double z, double startX, double startZ,
			double endX, double endZ) {
		double vx = endX - startX;
		double vz = endZ - startZ;
		double lengthSquared = vx * vx + vz * vz;
		if (lengthSquared < 1.0D) return Math.sqrt((x - startX) * (x - startX) + (z - startZ) * (z - startZ));
		double t = clamp01(((x - startX) * vx + (z - startZ) * vz) / lengthSquared);
		double dx = x - (startX + vx * t);
		double dz = z - (startZ + vz * t);
		return Math.sqrt(dx * dx + dz * dz);
	}

	/** Signed-ish distance for the broad central landmass. */
	private static double coreDistance(long seed, double x, double z) {
		double warpedX = x + (fbm(seed + 71L, x * 0.018D, z * 0.018D) - 0.5D) * 20.0D;
		double warpedZ = z + (fbm(seed + 97L, x * 0.018D - 4.0D, z * 0.018D + 8.0D) - 0.5D) * 20.0D;
		return Math.sqrt((warpedX / 132.0D) * (warpedX / 132.0D)
				+ (warpedZ / 110.0D) * (warpedZ / 110.0D));
	}

	/** True for the one connected landmass, including attached outcrops. */
	public static boolean isLand(long seed, double x, double z) {
		if (isCoastBay(seed, x, z)) return false;
		double edgeNoise = (fbm(seed + 131L, x * 0.022D, z * 0.022D) - 0.5D) * 0.22D;
		if (coreDistance(seed, x, z) < 1.0D + edgeNoise) return true;

		// These capsules begin at the center and end in broad, unequal outcrops.
		// They are the connective tissue that prevents disconnected floating plates.
		for (int i = 0; i < 6; i++) {
			double[] end = landmarkEnd(seed, i);
			double width = 21.0D + valueNoise(seed, 610 + i * 7, 43) * 16.0D;
			if (segmentDistance(x, z, end[0], end[1]) < width) return true;
		}
		return false;
	}

	/** Approximate radial boundary used by callers that need a readable extent. */
	public static double islandBoundaryRadius(long seed, double x, double z) {
		double r = Math.sqrt(x * x + z * z);
		if (r < 1.0D) return 150.0D;
		double directionX = x / r;
		double directionZ = z / r;
		double boundary = 122.0D + (fbm(seed + 131L, x * 0.022D, z * 0.022D) - 0.5D) * 28.0D;
		for (int i = 0; i < 6; i++) {
			double[] end = landmarkEnd(seed, i);
			double length = Math.sqrt(end[0] * end[0] + end[1] * end[1]);
			double dot = (directionX * end[0] + directionZ * end[1]) / length;
			if (dot > 0.82D) boundary = Math.max(boundary, length + 12.0D);
		}
		return boundary;
	}

	// ---------------------------------------------------------------------
	// Three-scale terrain height
	// ---------------------------------------------------------------------

	/** Target top-solid height for a land column. */	public static int heightAt(long seed, double x, double z) {
		double r = Math.sqrt(x * x + z * z);

		// Central Dragon site: a broad, gently undulating plateau at portal
		// height. It stays open for combat and never buries the exit portal.
		double rim = 40.0D + 7.0D * fbm(seed + 301L, x * 0.04D + 12.0D, z * 0.04D - 9.0D);
		if (r < rim) {
			double undulation = fbm(seed + 401L, x * 0.09D + 3.0D, z * 0.09D - 2.0D);
			return 59 + (int) Math.round((undulation - 0.5D) * 3.0D); // 58..61
		}

		double base = 58.0D;

		// Six major ridge arms. Tall and irregular: each arm has its own width,
		// height, and silhouette, and they are pushed outside the combat arena.
		for (int i = 0; i < 6; i++) {
			double[] end = landmarkEnd(seed, i);
			double distance = segmentDistance(x, z, end[0], end[1]);
			double width = 26.0D + valueNoise(seed, 610 + i * 7, 43) * 22.0D;
			double influence = 1.0D - smoothstep(width * 0.30D, width, distance);
			influence *= smoothstep(30.0D, 68.0D, r);
			double ridgeHeight = 18.0D + valueNoise(seed, 810 + i * 13, 47) * 22.0D;
			if (i == 0) ridgeHeight += 10.0D; // the Dragon's Crown
			base += influence * influence * ridgeHeight;
		}

		// A few steep spires: localized, very tall End Stone formations that
		// give the horizon a jagged silhouette. They never intrude on the arena.
		for (int i = 0; i < 3; i++) {
			double sx = (valueNoise(seed, 2100 + i * 31, 7) - 0.5D) * 220.0D;
			double sz = (valueNoise(seed, 13, 2100 + i * 31) - 0.5D) * 220.0D;
			if (sx * sx + sz * sz < 55.0D * 55.0D) continue;
			double radius = 6.0D + valueNoise(seed, 2200 + i, 2200) * 8.0D;
			double height = 28.0D + valueNoise(seed, 2300, 2300 + i) * 22.0D;
			double dx = x - sx;
			double dz = z - sz;
			double d = Math.sqrt(dx * dx + dz * dz);
			if (d < radius) base += height * (1.0D - d / radius);
		}

		// Three deep, enclosed basins: the Hollow and lower shelves.
		for (int i = 0; i < 3; i++) {
			double[] end = landmarkEnd(seed + 5000L, i + 2);
			double cx = end[0] * 0.62D;
			double cz = end[1] * 0.62D;
			double radius = 22.0D + valueNoise(seed, 1010 + i * 11, 53) * 26.0D;
			double drop = 14.0D + valueNoise(seed, 1110 + i * 13, 59) * 10.0D;
			double dx = x - cx;
			double dz = z - cz;
			double distance = Math.sqrt(dx * dx + dz * dz);
			double depression = 1.0D - smoothstep(radius * 0.40D, radius, distance);
			base -= depression * drop;
		}

		// Meso: continuous rolling relief across every region.
		base += (fbm(seed + 701L, x * 0.028D, z * 0.028D) - 0.5D) * 12.0D;
		base += (fbm(seed + 907L, x * 0.075D + 8.0D, z * 0.075D - 5.0D) - 0.5D) * 5.0D;

		// Steep void-facing cliffs.
		double edgeDistance = edgeDistance(seed, x, z);
		if (edgeDistance < 22.0D) {
			base -= (22.0D - edgeDistance) * 0.9D;
		}

		return (int) Math.round(Math.clamp(base, 38.0D, 108.0D));
	}

	private static double edgeDistance(long seed, double x, double z) {
		double r = Math.sqrt(x * x + z * z);
		if (r < 1.0D) return 140.0D;
		return islandBoundaryRadius(seed, x, z) - r;
	}

	// ---------------------------------------------------------------------
	// Fractures: few, off-center, meandering geological wounds
	// ---------------------------------------------------------------------

	/** Samples along two major scars and four shorter secondary fractures. */
	public static List<double[]> fracturePoints(long seed) {
		List<double[]> points = new ArrayList<>();
		RandomSource random = RandomSource.create(seed ^ 0x4B6F6E6553L);
		int count = 6;
		for (int i = 0; i < count; i++) {
			double startAngle = random.nextDouble() * Math.PI * 2.0D;
			double startRadius = i < 2 ? 52.0D + random.nextDouble() * 38.0D : 38.0D + random.nextDouble() * 58.0D;
			double x = Math.cos(startAngle) * startRadius;
			double z = Math.sin(startAngle) * startRadius;
			double heading = random.nextDouble() * Math.PI * 2.0D;
			double length = i < 2 ? 62.0D + random.nextDouble() * 44.0D : 28.0D + random.nextDouble() * 42.0D;
			double travelled = 0.0D;
			while (travelled < length) {
				if (!isLand(seed, x, z)) break;
				double width = (i < 2 ? 2.8D : 1.5D) + smoothNoise(seed, x * 0.08D, z * 0.08D) * 4.0D;
				points.add(new double[] { x, z, width });
				heading += (smoothNoise(seed + 17L, x * 0.11D, z * 0.11D) - 0.5D) * 0.8D;
				double step = 1.5D + smoothNoise(seed + 23L, x * 0.17D, z * 0.17D) * 1.5D;
				x += Math.cos(heading) * step;
				z += Math.sin(heading) * step;
				travelled += step;
			}
		}
		return points;
	}

	/** Minimum horizontal distance from a column to any fracture sample. */
	public static double distanceToFracture(long seed, double x, double z) {
		return distanceToNearest(fracturePoints(seed), x, z);
	}

	/** Minimum distance from (x, z) to any [x, z, ...] sample in {@code points}. */
	public static double distanceToNearest(java.util.List<double[]> points, double x, double z) {
		double nearest = Double.MAX_VALUE;
		for (double[] point : points) {
			double dx = x - point[0];
			double dz = z - point[1];
			nearest = Math.min(nearest, dx * dx + dz * dz);
		}
		return Math.sqrt(nearest);
	}

	/** Width attached to the nearest fracture sample. */
	public static double fractureWidth(long seed, double x, double z) {
		double nearest = Double.MAX_VALUE;
		double width = 1.5D;
		for (double[] point : fracturePoints(seed)) {
			double dx = x - point[0];
			double dz = z - point[1];
			double distance = dx * dx + dz * dz;
			if (distance < nearest) {
				nearest = distance;
				width = point[2];
			}
		}
		return width;
	}

	/** Sparse, incomplete ring arcs instead of a full circular pattern. */
	public static boolean ringArcPresent(long seed, double angleDegrees) {
		double radians = Math.toRadians(angleDegrees);
		double n = fbm(seed + 1601L, Math.cos(radians) * 3.0D + 5.0D,
				Math.sin(radians) * 3.0D - 3.0D);
		return n > 0.62D;
	}

	/** True when near a vanilla pillar base. */
	public static boolean isNearPillar(double x, double z, double margin) {
		for (int[] pillar : PILLAR_POSITIONS) {
			double dx = x - pillar[0];
			double dz = z - pillar[1];
			if (dx * dx + dz * dz < margin * margin) return true;
		}
		return false;
	}
}
