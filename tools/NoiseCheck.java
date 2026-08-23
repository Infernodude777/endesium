import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Mirrors EndesiumNoise so the biome selector can be validated without booting Minecraft. */
public class NoiseCheck {
    private static final double CELL_SIZE = 1800.0D;
    private static final int OCTAVES = 2;
    private static final double AMPLITUDE_FALLOFF = 0.5D;
    private static final double END_WASTES_THRESHOLD = 0.72D;
    private static final double CHORUS_WILDS_THRESHOLD = 0.70D;
    private static final long END_WASTES_SALT = 0x1D5E5DE5L;
    private static final long CHORUS_WILDS_SALT = 0x4C47A13AL;

    public static void main(String[] args) {
        long seed = 123456789L;
        int wastes = 0, wilds = 0, both = 0, total = 0;
        for (int z = -6400; z <= 6400; z += 4) {
            for (int x = -6400; x <= 6400; x += 4) {
                double d2 = x * (double) x + z * (double) z;
                if (d2 < 1024 * 1024) continue; // central island + dragon arena
                total++;
                if (isChorusWilds(seed, x, z)) {
                    wilds++;
                    if (isEndWastes(seed, x, z)) both++;
                } else if (isEndWastes(seed, x, z)) {
                    wastes++;
                }
            }
        }
        System.out.printf("total samples: %d%n", total);
        System.out.printf("end_wastes only: %d (%.2f%%)%n", wastes, 100.0 * wastes / total);
        System.out.printf("chorus_wilds only: %d (%.2f%%)%n", wilds, 100.0 * wilds / total);
        System.out.printf("both (wilds wins): %d (%.2f%%)%n", both, 100.0 * both / total);
        System.out.printf("any endesium: %d (%.2f%%)%n", wastes + wilds + both, 100.0 * (wastes + wilds + both) / total);
    }

    public static boolean isEndWastes(long worldSeed, int blockX, int blockZ) {
        return sample(worldSeed, blockX, blockZ, END_WASTES_SALT) > END_WASTES_THRESHOLD;
    }

    public static boolean isChorusWilds(long worldSeed, int blockX, int blockZ) {
        return sample(worldSeed, blockX, blockZ, CHORUS_WILDS_SALT) > CHORUS_WILDS_THRESHOLD;
    }

    private static double sample(long worldSeed, double blockX, double blockZ, long salt) {
        double sum = 0.0D;
        double amplitude = 1.0D;
        double total = 0.0D;
        double cell = CELL_SIZE;
        for (int octave = 0; octave < OCTAVES; octave++) {
            long octaveSeed = mix(worldSeed ^ salt, octave);
            sum += amplitude * valueNoise(octaveSeed, blockX / cell, blockZ / cell);
            total += amplitude;
            amplitude *= AMPLITUDE_FALLOFF;
            cell *= AMPLITUDE_FALLOFF;
        }
        return sum / total;
    }

    private static double valueNoise(long seed, double u, double v) {
        int cellX = floor(u);
        int cellZ = floor(v);
        double fracX = u - cellX;
        double fracZ = v - cellZ;
        double sx = smoothstep(fracX);
        double sz = smoothstep(fracZ);
        double v00 = hash01(seed, cellX, cellZ);
        double v10 = hash01(seed, cellX + 1, cellZ);
        double v01 = hash01(seed, cellX, cellZ + 1);
        double v11 = hash01(seed, cellX + 1, cellZ + 1);
        double a = v00 + (v10 - v00) * sx;
        double b = v01 + (v11 - v01) * sx;
        return a + (b - a) * sz;
    }

    private static double smoothstep(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static double hash01(long seed, int x, int z) {
        return (mix(seed, x * 0x9E3779B97F4A7C15L ^ z * 0xBF58476D1CE4E5B9L) >>> 11) * (1.0D / 9007199254740992.0D);
    }

    private static long mix(long value, long extra) {
        long h = value ^ extra;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }
}
