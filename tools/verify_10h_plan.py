import json, os

with open('jimbibo_10h_full_plan.json', encoding='utf-8') as f:
    plan = json.load(f)

root = plan['root'].replace('\\', '/').rstrip('/')
ok = 0
bad = []
for entry in plan['files']:
    path = (root + '/' + entry['path']).replace('//', '/')
    if not os.path.exists(path):
        bad.append(entry['path'] + ' (missing)')
        continue
    with open(path, encoding='utf-8') as fh:
        disk = fh.read().replace('\r\n', '\n')
    code = entry['code'].replace('\r\n', '\n')
    if disk == code:
        ok += 1
    else:
        bad.append(entry['path'])

print(f"match: {ok} of {len(plan['files'])}")
for b in bad:
    print(' ', b)
