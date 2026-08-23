// Quick threshold/cell-size tuning sim for the Endesium biome value noise.
// Mirrors the Java implementation in EndesiumNoise so thresholds can be
// validated before baking them into the mixin.
const M = 0x9E3779B97F4A7C15n;

function hash64(seed, x, z) {
  let h = BigInt.asUintN(64, seed ^ (BigInt(x) * 0x9E3779B97F4A7C15n) ^ (BigInt(z) * 0xBF58476D1CE4E5B9n));
  h = BigInt.asUintN(64, h ^ (h >> 30n));
  h = BigInt.asUintN(64, h * 0xBF58476D1CE4E5B9n);
  h = BigInt.asUintN(64, h ^ (h >> 27n));
  h = BigInt.asUintN(64, h * 0x94D049BB133111EBn);
  h = BigInt.asUintN(64, h ^ (h >> 31n));
  return h;
}

function hash01(seed, x, z) {
  return Number(hash64(seed, x, z) >> 11n) / 9007199254740992.0; // [0,1)
}

function smooth(t) {
  return t * t * (3 - 2 * t);
}

function valueNoise(seed, x, z, cell) {
  const cx = Math.floor(x / cell);
  const cz = Math.floor(z / cell);
  const fx = (x / cell) - cx;
  const fz = (z / cell) - cz;
  const sx = smooth(fx);
  const sz = smooth(fz);
  const v00 = hash01(seed, cx, cz);
  const v10 = hash01(seed, cx + 1, cz);
  const v01 = hash01(seed, cx, cz + 1);
  const v11 = hash01(seed, cx + 1, cz + 1);
  const a = v00 + (v10 - v00) * sx;
  const b = v01 + (v11 - v01) * sx;
  return a + (b - a) * sz;
}

function sample(seed, x, z, salt, cell, octaves) {
  let total = 0;
  let amp = 1;
  let sum = 0;
  let c = cell;
  for (let i = 0; i < octaves; i++) {
    sum += amp * valueNoise(hash64(seed, salt, i), x / c, z / c, 1);
    total += amp;
    amp *= 0.5;
    c *= 0.5;
  }
  return sum / total;
}

function coverage(seed, salt, cell, octaves, threshold, n, radius) {
  let count = 0;
  for (let i = 0; i < n; i++) {
    // uniform over a large square; outer-End rings make real coverage lower,
    // so a higher measured fraction is acceptable.
    const x = (Math.random() * 2 - 1) * radius;
    const z = (Math.random() * 2 - 1) * radius;
    if (sample(seed, x, z, salt, cell, octaves) > threshold) count++;
  }
  return (count / n) * 100;
}

const seed = 12345n;
for (const cell of [1400, 1800, 2200]) {
  for (const threshold of [0.68, 0.72, 0.76]) {
    const cov = coverage(seed, 0xE5D, cell, 2, threshold, 200000, 60000);
    console.log(`cell=${cell} thr=${threshold} -> ${cov.toFixed(2)}%`);
  }
}
