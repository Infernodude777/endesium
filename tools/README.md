# Local development tools

This directory contains local development tooling that is intentionally not versioned with Endesium.

## DogSprite

DogSprite is the free, open-source, local pixel-art editor and MCP server used for Endesium textures.

Install or refresh the official checkout:

```bash
git clone https://github.com/systemcrash92/DogSprite.git tools/DogSprite
```

Build the editor:

```bash
cd tools/DogSprite
npm install
npm run build
```

Build the MCP server:

```bash
cd tools/DogSprite/mcp-server
npm install
npm run build
```

The project-root `.mcp.json` starts the server with:

```text
node tools/DogSprite/mcp-server/dist/index.js
```

DogSprite MCP exports files to `tools/DogSprite/mcp-server/output/`. Validate approved PNGs there before copying them into Endesium's resource directories.
