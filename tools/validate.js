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

// ---- files nobody points at ----
for (const f of fs.readdirSync(DIR)) {
  if (f === 'index.json' || f === 'README.md') continue;
  if (!files.has(f)) fail(`${f} is in the folder but not listed in index.json`);
}

if (problems.length) {
  console.error(`\n${problems.length} problem(s):\n`);
  for (const p of problems) console.error('  - ' + p);
  console.error('');
  process.exit(1);
}
console.log(`OK - ${index.extensions.length} extensions, every file present, every hash matched.`);
