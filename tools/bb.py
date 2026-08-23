#!/usr/bin/env python3
"""
Blockbench MCP driver — calls tools on the local Blockbench MCP bridge
(http://localhost:3000/bb-mcp) via JSON-RPC. Manages the Mcp-Session-Id.

Usage:
  python3 tools/bb.py <tool_name> '<params-json>'
  python3 tools/bb.py list-tools
  python3 tools/bb.py init

The session id is cached in a temp file so sequential calls reuse it.
"""
import json
import os
import subprocess
import sys
import tempfile
import urllib.request

URL = "http://localhost:3000/bb-mcp"
STATE = os.path.join(tempfile.gettempdir(), "bb_mcp_session.json")


def load_state():
    try:
        with open(STATE) as f:
            return json.load(f)
    except Exception:
        return {}


def save_state(state):
    with open(STATE, "w") as f:
        json.dump(state, f)


def post(payload, session_id=None):
    data = json.dumps(payload).encode()
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
    }
    if session_id:
        headers["Mcp-Session-Id"] = session_id
    req = urllib.request.Request(URL, data=data, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            raw = resp.read().decode()
            return (json.loads(raw) if raw.strip() else {}), resp.headers
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        return (json.loads(raw) if raw.strip() else {}), e.headers


def call_tool(name, params, retries=2):
    state = load_state()
    sid = state.get("session_id")
    for attempt in range(retries + 1):
        body, headers = post({"jsonrpc": "2.0", "id": 1, "method": "tools/call",
                              "params": {"name": name, "arguments": params}}, sid)
        if "error" in body and "Mcp-Session-Id" in body["error"].get("message", ""):
            sid = None  # session gone; re-init below
        if not sid:
            init, h = post({"jsonrpc": "2.0", "id": 1, "method": "initialize",
                            "params": {"protocolVersion": "2024-11-05",
                                       "capabilities": {},
                                       "clientInfo": {"name": "endesium-tools", "version": "1.0"}}})
            sid = h.get("Mcp-Session-Id")
            save_state({"session_id": sid})
            post({"jsonrpc": "2.0", "method": "notifications/initialized", "params": {}}, sid)
        if "error" in body:
            # if server is unhappy with session, clear and retry once
            if attempt < retries and ("session" in str(body.get("error", {})).lower()):
                sid = None
                continue
            return body
        return body
    return body


def list_tools():
    state = load_state()
    sid = state.get("session_id")
    body, headers = post({"jsonrpc": "2.0", "id": 1, "method": "tools/list", "params": {}}, sid)
    if "error" in body:
        init, h = post({"jsonrpc": "2.0", "id": 1, "method": "initialize",
                        "params": {"protocolVersion": "2024-11-05", "capabilities": {},
                                   "clientInfo": {"name": "endesium-tools", "version": "1.0"}}})
        sid = h.get("Mcp-Session-Id")
        save_state({"session_id": sid})
        body, headers = post({"jsonrpc": "2.0", "id": 1, "method": "tools/list", "params": {}}, sid)
    return body


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 1
    if sys.argv[1] == "list-tools":
        body = list_tools()
        for t in body.get("result", {}).get("tools", []):
            print("-", t["name"])
        return 0
    if sys.argv[1] == "schema":
        body = list_tools()
        names = set(sys.argv[2:]) if len(sys.argv) > 2 else None
        for t in body.get("result", {}).get("tools", []):
            if names and t["name"] not in names:
                continue
            print("=" * 30, t["name"])
            print(json.dumps(t.get("inputSchema", {}))[:1500])
        return 0
    if sys.argv[1] == "init":
        state = load_state()
        print("cached session:", state.get("session_id"))
        return 0
    tool = sys.argv[1]
    params = json.loads(sys.argv[2]) if len(sys.argv) > 2 else {}
    body = call_tool(tool, params)
    print(json.dumps(body, indent=2)[:4000])
    return 0


if __name__ == "__main__":
    sys.exit(main())
