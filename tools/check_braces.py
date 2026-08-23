import re
import sys
import pathlib

ROOT = pathlib.Path('src')
bad = []
for f in list(ROOT.rglob('*Client/java/**/*.java')) + list(ROOT.rglob('*main/java/**/*.java')) + list(ROOT.rglob('*test/java/**/*.java')):
    src = f.read_text(encoding='utf-8', errors='replace')
    s = re.sub(r'"(?:[^"\\]|\\.)*"', '""', src)
    s = re.sub(r"'(?:[^'\\]|\\.)'", "''", s)
    s = re.sub(r'//.*', '', s)
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
    for open_c, close_c in [('{', '}'), ('(', ')'), ('[', ']')]:
        d = s.count(open_c) - s.count(close_c)
        if d != 0:
            bad.append((str(f).replace('\\', '/'), open_c + close_c, d))
if bad:
    for b in bad:
        print('UNBALANCED:', b)
    sys.exit(1)
print('all java files balanced')
