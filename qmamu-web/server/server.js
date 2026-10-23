/**
 * Qmamu meta-search backend.
 *
 * Zero dependencies: it uses only Node's http module and the global fetch
 * (Node 18+). It asks a self-hosted SearXNG instance for results, merges the
 * duplicates, and serves the Qmamu frontend.
 *
 *   GET /api/search?q=<query>&engines=google,bing&category=general&page=1
 *
 * Env:
 *   PORT         port to listen on              (default 8080)
 *   SEARXNG_URL  base url of the SearXNG server (default http://searxng:8080)
 *   CACHE_MS     how long to reuse a response   (default 60000)
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const { URL } = require('url');

const PORT = parseInt(process.env.PORT || '8080', 10);
const SEARXNG = (process.env.SEARXNG_URL || 'http://searxng:8080').replace(/\/+$/, '');
const CACHE_MS = parseInt(process.env.CACHE_MS || '60000', 10);
const WEB = path.join(__dirname, '..', 'web');

// ---------------------------------------------------------------------------
// engines the UI offers, mapped to SearXNG engine names
// ---------------------------------------------------------------------------
const ENGINES = {
  google: { label: 'Google', searx: ['google'] },
  bing: { label: 'Bing', searx: ['bing'] },
  ddg: { label: 'DuckDuckGo', searx: ['duckduckgo'] },
  brave: { label: 'Brave', searx: ['brave'] },
  yandex: { label: 'Yandex', searx: ['yandex'] },
  startpage: { label: 'Startpage', searx: ['startpage'] },
  mojeek: { label: 'Mojeek', searx: ['mojeek'] },
  wikipedia: { label: 'Wikipedia', searx: ['wikipedia'] }
};

const CATEGORIES = ['general', 'images', 'videos', 'news', 'map', 'it', 'science'];

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon',
  '.webmanifest': 'application/manifest+json'
};

// ---------------------------------------------------------------------------
// tiny in-memory cache
// ---------------------------------------------------------------------------
const cache = new Map();
function cacheGet(key) {
  const hit = cache.get(key);
  if (!hit) return null;
  if (Date.now() - hit.at > CACHE_MS) { cache.delete(key); return null; }
  return hit.value;
}
function cachePut(key, value) {
  if (cache.size > 300) cache.clear();
  cache.set(key, { at: Date.now(), value });
}

// ---------------------------------------------------------------------------
// de-duplication
// ---------------------------------------------------------------------------
function normaliseUrl(raw) {
  try {
    const u = new URL(raw);
    u.hash = '';
    for (const k of [...u.searchParams.keys()]) {
      if (/^(utm_|fbclid$|gclid$|ref$|ref_src$|source$|si$)/i.test(k)) u.searchParams.delete(k);
    }
    const q = u.searchParams.toString();
    return (u.origin + u.pathname.replace(/\/+$/, '')).toLowerCase() + (q ? '?' + q.toLowerCase() : '');
  } catch (e) {
    return String(raw).toLowerCase();
  }
}

function titleKey(t) {
  return String(t || '').toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim().slice(0, 70);
}

/**
 * Merge results that point at the same page (or share a title) and collect the
 * list of engines that returned each one.
 */
function dedupe(rows) {
  const byUrl = new Map();
  const byTitle = new Map();
  const out = [];

  for (const r of rows) {
    if (!r || !r.url) continue;
    const url = normaliseUrl(r.url);
    const tk = titleKey(r.title);
    const engines = (r.engines && r.engines.length ? r.engines : [r.engine]).filter(Boolean);

    let hit = byUrl.get(url) || (tk ? byTitle.get(tk) : null);
    if (hit) {
      for (const e of engines) if (!hit.engines.includes(e)) hit.engines.push(e);
      if ((r.content || '').length > (hit.snippet || '').length) hit.snippet = r.content;
      if (!hit.thumbnail && r.thumbnail) hit.thumbnail = r.thumbnail;
      continue;
    }

    const item = {
      title: r.title || r.url,
      url: r.url,
      snippet: r.content || '',
      engines,
      thumbnail: r.thumbnail || '',
      publishedDate: r.publishedDate || null
    };
    byUrl.set(url, item);
    if (tk) byTitle.set(tk, item);
    out.push(item);
  }
  return out;
}

// ---------------------------------------------------------------------------
// SearXNG call
// ---------------------------------------------------------------------------
async function searxSearch({ q, engines, category, page, lang }) {
  const url = new URL(SEARXNG + '/search');
  url.searchParams.set('q', q);
  url.searchParams.set('format', 'json');
  url.searchParams.set('categories', category || 'general');
  url.searchParams.set('pageno', String(page || 1));
  if (lang) url.searchParams.set('language', lang);
  if (engines && engines.length) url.searchParams.set('engines', engines.join(','));

  const ctl = new AbortController();
  const timer = setTimeout(() => ctl.abort(), 12000);
  try {
    const res = await fetch(url, {
      signal: ctl.signal,
      headers: { 'Accept': 'application/json', 'User-Agent': 'Qmamu/1.0 (+personal use)' }
    });
    if (!res.ok) throw new Error('searxng http ' + res.status);
    return await res.json();
  } finally {
    clearTimeout(timer);
  }
}

// ---------------------------------------------------------------------------
// request handling
// ---------------------------------------------------------------------------
async function handleSearch(req, res, parsed) {
  const q = (parsed.searchParams.get('q') || '').trim();
  const category = (parsed.searchParams.get('category') || 'general').toLowerCase();
  const page = Math.max(1, parseInt(parsed.searchParams.get('page') || '1', 10) || 1);
  const lang = parsed.searchParams.get('lang') || '';
  const wanted = (parsed.searchParams.get('engines') || '')
    .split(',').map(s => s.trim()).filter(s => ENGINES[s]);

  if (!q) return sendJson(res, 400, { error: 'missing q' });
  if (!CATEGORIES.includes(category)) return sendJson(res, 400, { error: 'bad category' });

  // an empty engine list means "let SearXNG use all of them"
  const searxEngines = wanted.flatMap(id => ENGINES[id].searx);

  const key = [q, category, page, lang, searxEngines.join('+')].join('|');
  const cached = cacheGet(key);
  if (cached) return sendJson(res, 200, { ...cached, cached: true });

  const started = Date.now();
  let data;
  try {
    data = await searxSearch({ q, engines: searxEngines, category, page, lang });
  } catch (e) {
    return sendJson(res, 502, {
      error: 'search backend unreachable',
      detail: String(e.message || e),
      hint: 'Is the SearXNG container up and is json enabled in its settings.yml?'
    });
  }

  const merged = dedupe(data.results || []);

  // how many hits each engine contributed, before de-duplication
  const perEngine = {};
  for (const r of data.results || []) {
    for (const e of (r.engines && r.engines.length ? r.engines : [r.engine]).filter(Boolean)) {
      perEngine[e] = (perEngine[e] || 0) + 1;
    }
  }

  const payload = {
    query: q,
    category,
    page,
    engines: wanted,
    results: merged,
    raw: (data.results || []).length,
    merged: merged.length,
    perEngine,
    tookMs: Date.now() - started,
    answers: data.answers || [],
    infoboxes: data.infoboxes || [],
    suggestions: data.suggestions || [],
    cached: false
  };
  cachePut(key, payload);
  sendJson(res, 200, payload);
}

function sendJson(res, code, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(code, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-store'
  });
  res.end(body);
}

function serveStatic(res, pathname) {
  let rel = pathname === '/' ? '/index.html' : pathname;
  const full = path.join(WEB, path.normalize(rel).replace(/^(\.\.[/\\])+/, ''));
  if (!full.startsWith(WEB)) return notFound(res);
  fs.readFile(full, (err, buf) => {
    if (err) return notFound(res);
    const ext = path.extname(full).toLowerCase();
    res.writeHead(200, {
      'Content-Type': MIME[ext] || 'application/octet-stream',
      'Content-Length': buf.length,
      'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=3600'
    });
    res.end(buf);
  });
}

function notFound(res) {
  const body = 'Not found';
  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8', 'Content-Length': Buffer.byteLength(body) });
  res.end(body);
}

// ---------------------------------------------------------------------------
// self test:  node server.js --selftest
// ---------------------------------------------------------------------------
if (process.argv.includes('--selftest')) {
  const rows = [
    { title: 'Hello World', url: 'https://example.com/a?utm_source=x', content: 'short', engine: 'google' },
    { title: 'Hello World!', url: 'https://example.com/a/', content: 'a much longer snippet from bing', engine: 'bing' },
    { title: 'Other page', url: 'https://other.com/p', content: 'x', engine: 'duckduckgo' },
    { title: 'Other page', url: 'https://other.com/p#frag', content: 'y', engine: 'brave' },
    { title: 'Third', url: 'https://third.com/', content: 'z', engines: ['yandex', 'mojeek'] }
  ];
  const out = dedupe(rows);
  const checks = [
    ['merges by url and by title', out.length === 3],
    ['keeps both engines', out[0].engines.indexOf('google') >= 0 && out[0].engines.indexOf('bing') >= 0],
    ['keeps the longer snippet', out[0].snippet.indexOf('bing') > 0],
    ['merges fragments', out[1].engines.length === 2],
    ['keeps an engines array', out[2].engines.length === 2],
    ['strips tracking params', normaliseUrl('https://example.com/a?utm_source=x') === 'https://example.com/a']
  ];
  let bad = 0;
  for (const [name, pass] of checks) {
    console.log((pass ? 'PASS  ' : 'FAIL  ') + name);
    if (!pass) bad++;
  }
  console.log(bad === 0 ? '\nAll checks passed (' + out.length + ' merged results).' : '\n' + bad + ' check(s) failed.');
  process.exit(bad === 0 ? 0 : 1);
}

const server = http.createServer((req, res) => {
  const parsed = new URL(req.url, 'http://' + (req.headers.host || 'localhost'));

  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    return res.end();
  }

  if (parsed.pathname === '/api/search') return void handleSearch(req, res, parsed);

  if (parsed.pathname === '/api/engines') {
    return sendJson(res, 200, Object.entries(ENGINES).map(([id, e]) => ({ id, label: e.label })));
  }

  if (parsed.pathname === '/api/health') {
    return sendJson(res, 200, { ok: true, searxng: SEARXNG });
  }

  return serveStatic(res, parsed.pathname);
});

server.listen(PORT, () => {
  console.log('Qmamu meta-search listening on http://0.0.0.0:' + PORT);
  console.log('SearXNG backend: ' + SEARXNG);
});
