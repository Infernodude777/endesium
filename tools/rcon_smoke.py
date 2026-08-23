#!/usr/bin/env python3
"""Minimal RCON client for driving the smoke-test dedicated server."""
import socket, struct, sys

HOST, PORT = "127.0.0.1", 25575
PASSWORD = "smokepass"

def send_packet(sock, req_id, ptype, payload):
    data = struct.pack("<ii", req_id, ptype) + payload.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(data)) + data)

def read_packet(sock):
    raw_len = recv_exact(sock, 4)
    (length,) = struct.unpack("<i", raw_len)
    body = recv_exact(sock, length)
    rid, rtype = struct.unpack("<ii", body[:8])
    return rid, rtype, body[8:-2].decode("utf-8", errors="replace")

def recv_exact(sock, n):
    buf = b""
    while len(buf) < n:
        chunk = sock.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("socket closed")
        buf += chunk
    return buf

def main():
    cmds = sys.argv[1:]
    sock = socket.create_connection((HOST, PORT), timeout=20)
    send_packet(sock, 1, 3, PASSWORD)
    rid, rtype, _ = read_packet(sock)
    if rid == -1:
        print("RCON AUTH FAILED"); sys.exit(1)
    print("# auth ok")
    for i, cmd in enumerate(cmds, start=100):
        send_packet(sock, i, 2, cmd)
        rid, rtype, resp = read_packet(sock)
        print(f"> {cmd}\n{resp.strip()}")
    sock.close()

if __name__ == "__main__":
    main()
