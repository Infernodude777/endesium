const fs = require('fs');
const planPath = 'C:/Users/Nikhil/AppData/Local/Temp/jimbibo_pe_y6qit.plan.json';
const root = 'C:/Users/Nikhil/Desktop/endesium';

const plan = JSON.parse(fs.readFileSync(planPath, 'utf8'));

// 1. Replace the validator code with the corrected on-disk version.
const validator = fs.readFileSync(root + '/tools/validate_resources.mjs', 'utf8');
const v = plan.files.find(f => f.path.endsWith('validate_resources.mjs'));
if (v) { v.code = validator; v.summary = 'fix namespace resolution in asset validator'; }

// 2. Append the new reference docs, reading content from disk.
const additions = [
  { path: 'docs/DESIGN_DECISIONS.md',  summary: 'add architecture decision records',           time: 30 },
  { path: 'docs/CODE_WALKTHROUGH.md',  summary: 'add per-class code walkthrough',               time: 30 },
  { path: 'docs/ADVANCEMENTS.md',      summary: 'document advancements and loot',               time: 15 },
  { path: 'docs/WORLDGEN_REFERENCE.md', summary: 'document worldgen JSON reference',            time: 20 },
  { path: 'docs/AUDIO_VISUAL_DESIGN.md', summary: 'document audio and visual design',            time: 20 },
  { path: 'docs/TESTING_RUNBOOK.md',   summary: 'add step-by-step testing runbook',             time: 20 },
  { path: 'docs/BIOME_DESIGN.md',      summary: 'document wastes vs wilds biome contrast',      time: 20 },
];

for (const a of additions) {
  const code = fs.readFileSync(root + '/' + a.path, 'utf8');
  plan.files.push({ path: a.path, summary: a.summary, code, mode: 'replace', time: a.time });
}

fs.writeFileSync(planPath, JSON.stringify(plan, null, 2));
console.log('patched plan: files=', plan.files.length,
  'total_chars=', plan.files.reduce((n, f) => n + f.code.length, 0));
