#!/usr/bin/env python3
"""Add all MCP servers to global configs for VS Code, OpenCode, and Codex CLI."""
import json, os, pathlib

HOME = pathlib.Path(os.environ["USERPROFILE"])
APPDATA = pathlib.Path(os.environ["APPDATA"])

VSCODE_MCP = APPDATA / "Code" / "User" / "mcp.json"
OPENCODE_CONFIG = HOME / ".config" / "opencode" / "opencode.jsonc"
CODEX_CONFIG = HOME / ".codex" / "config.toml"

JIMBIBO_PATH = "C:/Users/Nikhil/Desktop/ai-typer/jimbibo_mcp.py"
DOGSPRITE_PATH = "C:/Users/Nikhil/Desktop/endesium/tools/DogSprite/mcp-server/dist/index.js"

# === 1. VS Code (GitHub Copilot + OpenCodex) ===
print("=== Updating VS Code global mcp.json ===")
vscode_cfg = json.loads(VSCODE_MCP.read_text(encoding="utf-8"))
servers = vscode_cfg.get("servers", {})

for name, cfg in {
    "blockbench": {
        "type": "stdio",
        "command": "node",
        "args": ["-e", "const http = require('http'); http.get('http://localhost:3000/bb-mcp')"]
    },
    "dogsprite-pixel-art": {
        "type": "stdio",
        "command": "node",
        "args": [DOGSPRITE_PATH]
    },
}.items():
    if name not in servers:
        servers[name] = cfg
        print(f"  + Added {name}")
    else:
        print(f"  = {name} already exists")

vscode_cfg["servers"] = servers
VSCODE_MCP.write_text(json.dumps(vscode_cfg, indent=2) + "\n", encoding="utf-8")
print(f"  Saved: {VSCODE_MCP}")

# === 2. OpenCode (text-based, not strict JSON) ===
print("\n=== Updating OpenCode global opencode.jsonc ===")
raw = OPENCODE_CONFIG.read_text(encoding="utf-8") if OPENCODE_CONFIG.exists() else '{\n  "$schema": "https://opencode.ai/config.json"\n}'

# Check if mcp section already exists
if '"mcp"' in raw or "'mcp'" in raw:
    print("  = mcp section already exists, skipping")
else:
    # Insert mcp block before the closing brace
    mcp_block = '''  "mcp": {
    "jimbibo": {
      "type": "stdio",
      "command": "python",
      "args": ["''' + JIMBIBO_PATH + '''"]
    },
    "blockbench": {
      "type": "stdio",
      "command": "node",
      "args": ["-e", "const http = require('http'); http.get('http://localhost:3000/bb-mcp')"]
    },
    "dogsprite-pixel-art": {
      "type": "stdio",
      "command": "node",
      "args": ["''' + DOGSPRITE_PATH + '''"]
    },
    "browseros": {
      "type": "http",
      "url": "http://127.0.0.1:9239/mcp"
    },
    "blender": {
      "type": "stdio",
      "command": "uvx",
      "args": ["blender-mcp"]
    }
  },\n'''

    # Find the last closing brace and insert before it
    last_brace = raw.rfind("}")
    if last_brace != -1:
        raw = raw[:last_brace] + mcp_block + raw[last_brace:]
    else:
        raw = '{\n' + mcp_block + '}'

    OPENCODE_CONFIG.write_text(raw, encoding="utf-8")
    print("  + Added mcp block with all 5 servers")
    print(f"  Saved: {OPENCODE_CONFIG}")

# === 3. Codex CLI ===
print("\n=== Updating Codex CLI config.toml ===")
codex_cfg = CODEX_CONFIG.read_text(encoding="utf-8") if CODEX_CONFIG.exists() else ""

if "[mcp_servers]" not in codex_cfg:
    new_section = """
[mcp_servers.jimbibo]
command = "python"
args = ["C:/Users/Nikhil/Desktop/ai-typer/jimbibo_mcp.py"]

[mcp_servers.blockbench]
command = "node"
args = ["-e", "const http = require('http'); http.get('http://localhost:3000/bb-mcp')"]

[mcp_servers.dogsprite-pixel-art]
command = "node"
args = ["C:/Users/Nikhil/Desktop/endesium/tools/DogSprite/mcp-server/dist/index.js"]

[mcp_servers.browseros]
command = "npx"
args = ["-y", "@anthropic/browseros-mcp"]
env = { MCP_URL = "http://127.0.0.1:9239/mcp" }

[mcp_servers.blender]
command = "uvx"
args = ["blender-mcp"]
"""
    CODEX_CONFIG.write_text(codex_cfg.rstrip() + "\n" + new_section, encoding="utf-8")
    print("  + Added mcp_servers section with all 5 servers")
else:
    print("  = mcp_servers section already exists")

print(f"  Saved: {CODEX_CONFIG}")

# === Summary ===
print("\n" + "=" * 60)
print("ALL MCP SERVERS ADDED TO GLOBAL CONFIGS:")
print("=" * 60)
print(f"\n{'Tool':<25} Config Path")
print(f"{'-'*25} {'-'*50}")
print(f"{'GitHub Copilot':<25} {VSCODE_MCP}")
print(f"{'OpenCodex':<25} {VSCODE_MCP} (same)")
print(f"{'OpenCode':<25} {OPENCODE_CONFIG}")
print(f"{'Codex CLI':<25} {CODEX_CONFIG}")
print(f"\n{'Server':<25} {'Type':<10} Where")
print(f"{'-'*25} {'-'*10} {'-'*30}")
print(f"{'Jimbibo (AI Typer)':<25} {'stdio':<10} VS Code + OpenCode + Codex")
print(f"{'Blockbench (3D)':<25} {'stdio':<10} VS Code + OpenCode + Codex")
print(f"{'DogSprite (Pixel)':<25} {'stdio':<10} VS Code + OpenCode + Codex")
print(f"{'BrowserOS':<25} {'http':<10} VS Code + OpenCode + Codex")
print(f"{'Blender':<25} {'stdio':<10} VS Code + OpenCode + Codex")
print("\nNote: Blockbench MCP needs localhost:3000/bb-mcp running first.")
