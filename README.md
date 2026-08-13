# Endesium

Endesium is a vanilla+ expansion of the Minecraft End dimension for Fabric 1.21.1. Its long-term focus is exploration, mystery, danger, and discovery while preserving Minecraft's visual language.

## Development foundation

The project targets Minecraft 1.21.1, Fabric, and Java 21. The first foundation milestone contains only registry, datagen, and dependency smoke tests; End generation and gameplay systems are intentionally unchanged.

Build the project with:

```bash
./gradlew build
```

Run the data generator with:

```bash
./gradlew runDatagen
```

Run the development client with:

```bash
./gradlew runClient
```

Run the dedicated server with:

```bash
./gradlew runServer
```

## License

This project is currently distributed under the CC0 license inherited from the Fabric template.
