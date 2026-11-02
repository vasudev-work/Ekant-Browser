#!/usr/bin/env node
/*
 * Checks the extension store before it ships.
 *
 * Run:  node tools/validate.js
 * Exit: 0 everything is consistent, 1 something is wrong (printed with a path).
 *
 * Catches the things that have actually gone wrong here: an index that lost
 * entries, a file the index names but that is not there, a hash that no longer
 * matches the script, and a script that will not parse.
 */
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { execFileSync } = require('child_process');

const ROOT = path.join(__dirname, '..');
const DIR = path.join(ROOT, 'extensions');
const INDEX = path.join(DIR, 'index.json');

const problems = [];
const fail = (m) => problems.push(m);

function sha256(text) {
  return crypto.createHash('sha256').update(text, 'utf8').digest('hex');
}

// ---- index ----
let index;
try {
  index = JSON.parse(fs.readFileSync(INDEX, 'utf8'));
} catch (e) {
  console.error('extensions/index.json is not valid JSON:', e.message);
  process.exit(1);
}
if (!Array.isArray(index.extensions) || index.extensions.length === 0) {
  fail('index.json has no extensions array, or it is empty');
}
if (index.schema === undefined) fail('index.json is missing "schema"');

const seen = new Set();
const files = new Set();

for (const e of index.extensions || []) {
  const id = e.id || '(no id)';
  for (const key of ['id', 'name', 'file', 'pattern', 'version']) {
    if (!e[key] || String(e[key]).trim() === '') fail(`${id}: missing "${key}"`);
  }
  if (e.id) {
    if (seen.has(e.id)) fail(`${id}: duplicate id`);
    seen.add(e.id);
  }
  if (!e.file) continue;
  if (files.has(e.file)) fail(`${id}: ${e.file} is listed twice`);
  files.add(e.file);

  const p = path.join(DIR, e.file);
  if (!fs.existsSync(p)) { fail(`${id}: ${e.file} does not exist`); continue; }

  const text = fs.readFileSync(p, 'utf8');
  if (e.sha256) {
    const got = sha256(text);
    if (got.toLowerCase() !== String(e.sha256).toLowerCase()) {
      fail(`${id}: sha256 mismatch - index says ${e.sha256.slice(0, 12)}..., file is ${got.slice(0, 12)}...`);
    }
  } else {
    fail(`${id}: no sha256 in the index`);
  }
  if (/\bchrome\s*\./.test(text)) fail(`${id}: uses chrome.*, which cannot work in WebView`);

  try {
    execFileSync(process.execPath, ['--check', p], { stdio: 'pipe' });
  } catch (err) {
    fail(`${id}: ${e.file} does not parse - ${String(err.stderr || err.message).split('\n')[0]}`);
  }
}

// ---- signature ----
// The browser refuses a list that is not signed with the store's private key,
// so a stale or hand-edited index.json would break every install. Catch it here.
try {
  const sigFile = path.join(DIR, 'index.json.sig');
  const pubFile = path.join(DIR, 'store-pubkey.pem');
  if (!fs.existsSync(sigFile)) fail('extensions/index.json.sig is missing - run tools/sign.py');
  else if (fs.existsSync(pubFile)) {
    const crypto2 = require('crypto');
    const body = fs.readFileSync(INDEX);
    const sig = Buffer.from(fs.readFileSync(sigFile, 'utf8').trim(), 'base64');
    const ok = crypto2.verify('sha256', body, fs.readFileSync(pubFile), sig);
    if (!ok) fail('index.json.sig does not match index.json - re-run tools/sign.py');
  }
} catch (e) {
  fail('could not check the signature: ' + e.message);
}

// ---- files nobody points at ----
for (const f of fs.readdirSync(DIR)) {
  if (f === 'index.json' || f === 'index.json.sig' || f === 'store-pubkey.pem' || f === 'README.md') continue;
  if (!files.has(f)) fail(`${f} is in the folder but not listed in index.json`);
}

if (problems.length) {
  console.error(`\n${problems.length} problem(s):\n`);
  for (const p of problems) console.error('  - ' + p);
  console.error('');
  process.exit(1);
}
console.log(`OK - ${index.extensions.length} extensions, every file present, every hash matched.`);
