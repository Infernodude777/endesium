# Compile Fix Notes - WorldGenRegion

## Symptom

```
src/main/java/com/infernodude777/endesium/world/StructurePlacement.java:8:
  error: cannot find symbol
  import net.minecraft.world.level.chunk.WorldGenRegion;
src/main/java/com/infernodude777/endesium/world/StructurePlacement.java:50:
  error: cannot find symbol
  if (level instanceof WorldGenRegion region) {
```

## Root cause

In 1.21.1 with Mojang (official) mappings, `WorldGenRegion` is not in
`net.minecraft.world.level.chunk`. It lives in the server package:

```
net.minecraft.server.level.WorldGenRegion
```

The class implements `net.minecraft.world.level.WorldGenLevel`, so the
`instanceof` pattern in `StructurePlacement.set` is valid once the import is
correct. `getCenter()` returns `net.minecraft.world.level.ChunkPos`, which the
file already imported.

## Fix

- `import net.minecraft.world.level.chunk.WorldGenRegion;`
  -> `import net.minecraft.server.level.WorldGenRegion;`
- Added a javadoc note on `isWithinGeneratingRegion` recording the correct
  package so the mistake cannot be reintroduced.

## How to confirm

```
./gradlew compileJava
```

Expect `BUILD SUCCESSFUL` with no errors. The P2-6 write gate
(`isWithinGeneratingRegion`) is unchanged in behavior: landmark writes are
still refused outside the currently generating 3x3 chunk region.