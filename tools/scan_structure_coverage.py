#!/usr/bin/env python3
"""
Structure coverage scanner for Endesium.

Drives a RUNNING dedicated server over RCON (enable rcon in
run/server.properties) and verifies that every registered Endesium structure
is locatable from the world origin in the End dimension. Reports distances so
balance passes can record real spacing instead of intuition.

Usage:
    python tools/scan_structure_coverage.py [--host 127.0.0.1] [--port 25575]
                                            [--password smokepass]

Exit code 0 = all twenty structures located; 1 = one or more missing.
"""
import argparse
import socket
import struct
import sys

STRUCTURES = [
    # Flagships
    "dust_cathedral", "elderwood_sanctum", "skyrend_keep", "drowned_cathedral",
    "lumen_cathedral", "great_caldera", "sunken_geode", "void_spire",
    "crown_observatory", "null_archive",
    # Landmarks
    "dune_fossil_arch", "hollow_stump", "windvane_watchtower", "mire_bell_cairn",
    "lightwell_gazebo", "ember_shrine", "shard_spire_cluster", "anchor_ruin",
    "needle_circle", "null_obelisk",
]


class Rcon:
    def __init__(self, host, port, password):
        self.sock = socket.create_connection((host, port), timeout=30)
        self._send(1, 3, password)
        rid, _, _ = self._recv()
        if rid == -1:
            raise RuntimeError("RCON auth failed")

    def _send(self, rid, ptype, payload):
        data = struct.pack("<ii", rid, ptype) + payload.encode() + b"\x00\x00"
        self.sock.sendall(struct.pack("<i", len(data)) + data)

    def _recv_exact(self, n):
        buf = b""
        while len(buf) < n:
            chunk = self.sock.recv(n - len(buf))
            if not chunk:
                raise ConnectionError("socket closed")
            buf += chunk
        return buf

    def _recv(self):
        (length,) = struct.unpack("<i", self._recv_exact(4))
        body = self._recv_exact(length)
        rid, rtype = struct.unpack("<ii", body[:8])
        return rid, rtype, body[8:-2].decode("utf-8", errors="replace")

    def command(self, cmd):
        self._send(100, 2, cmd)
        _, _, resp = self._recv()
        return resp.strip()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=25575)
    ap.add_argument("--password", default="smokepass")
    args = ap.parse_args()

    rcon = Rcon(args.host, args.port, args.password)
    found, missing = [], []
    for name in STRUCTURES:
        resp = rcon.command(
            f"execute in minecraft:the_end run locate structure endesium:{name}")
        if "is at" in resp.lower():
            found.append((name, resp))
            print(f"[OK]   {name}: {resp.split('is at')[-1].strip()}")
        else:
            missing.append(name)
            print(f"[MISS] {name}: {resp}")

    print(f"\nlocated {len(found)}/{len(STRUCTURES)}")
    if missing:
        print("missing: " + ", ".join(missing))
        sys.exit(1)


if __name__ == "__main__":
    main()
