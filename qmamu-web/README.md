# Qmamu — a personal meta-search engine

Qmamu is your **own frontend** for web search. It asks several engines at once
(Google, Bing, DuckDuckGo, Brave, Yandex, Startpage, Mojeek), merges the
duplicate hits, and renders everything in Qmamu's own UI. No iframes, no
redirects, and **no paid APIs or keys**.

```
browser  ──►  Qmamu web page  ──►  Qmamu server  ──►  SearXNG  ──►  Google / Bing / …
 (your UI)      /api/search        merge + dedupe      keyless         DuckDuckGo / …
```

## The stack, and why

| Piece | Choice | Why |
|---|---|---|
| Aggregation | **SearXNG** (self-hosted, Docker) | Already fetches ~70 engines keyless, handles the scraping, and exposes a JSON API. No API keys, no billing. |
| Merge + serve | **Qmamu server** (Node, zero dependencies) | De-duplicates across engines, labels which engine returned what, caches briefly, and serves the frontend on the same origin (so no CORS pain). |
| UI | **Qmamu web page** (plain HTML/CSS/JS) | Your own look. Nothing is loaded from a third party. |

SearXNG's own frontend is fine, but it is *its* UI. Here it is used only as the
**JSON backend** so Qmamu can own the page.

Other options and where they fit:

- **ddgs** (Python library) — great for a script or a small bot; you would have to
  write the retry/dedupe/rate-limit logic yourself.
- **OpenSERP**, **4get** — lighter alternatives to SearXNG; fewer engines and
  fewer maintenance eyes on them.
- **Rolling your own scrapers** — most control, most breakage. Engines change
  their HTML often and start serving CAPTCHAs quickly.

For a personal engine, SearXNG behind your own UI is the least work per unit of
reliability.

## Quick start (Docker)

```bash
cd qmamu-web
docker compose up -d
# open http://localhost:8080
```

That brings up two containers: `qmamu-searxng` (the fetcher) and `qmamu-server`
(the merger + web server). The first start takes a minute while the images pull.

Before exposing it to anything but your own machine, change the two
`change-this-to-a-long-random-string` values in `docker-compose.yml` and
`searxng/settings.yml`.

## Run without Docker

You still need SearXNG somewhere (Docker is by far the easiest), then:

```bash
cd qmamu-web/server
SEARXNG_URL=http://localhost:8081 PORT=8080 node server.js
```

Requirements: Node 18 or newer (for the built-in `fetch`).

## API

The server exposes three endpoints.

**`GET /api/search`**

| Param | Meaning |
|---|---|
| `q` | the query (required) |
| `engines` | comma-separated ids: `google,bing,ddg,brave,yandex,startpage,mojeek,wikipedia`. Omit for all enabled engines. |
| `category` | `general`, `images`, `videos`, `news`, `map`, `it`, `science` |
| `page` | page number, from 1 |
| `lang` | optional language code |

```bash
curl "http://localhost:8080/api/search?q=isro&engines=google,ddg&category=general"
```

```json
{
  "query": "isro",
  "results": [
    { "title": "…", "url": "…", "snippet": "…", "engines": ["google", "duckduckgo"] }
  ],
  "raw": 42,
  "merged": 28,
  "perEngine": { "google": 20, "duckduckgo": 22 },
  "tookMs": 640,
  "cached": false
}
```

`raw` is how many hits came back, `merged` how many survived de-duplication,
and each result carries every engine that returned it.

**`GET /api/engines`** — the engine ids the UI can use.
**`GET /api/health`** — a liveness check that also echoes the SearXNG URL.

## How de-duplication works

1. Tracking parameters (`utm_*`, `fbclid`, `gclid`, `ref`, …) and fragments are
   stripped, so `…/a?utm_source=x` and `…/a/` are recognised as one page.
2. Results are merged when the normalised URL matches, **or** when the titles
   match after punctuation is removed.
3. The longer snippet wins, a thumbnail is kept if any engine supplied one, and
   every contributing engine is recorded on the result.

Run `node server/server.js --selftest` to exercise that logic on its own.

## The frontend

`web/` is a static page. It reads the query from the URL, so a link like

```
/?q=isro&engine=google,ddg&cat=news
```

opens with those engines and that tab already selected — which is how the Android
app hands a query over.

By default it calls `/api/search` on its own origin. To point it at a backend
somewhere else, set this before `app.js` loads:

```html
<script>window.QMAMU_API = 'https://search.example.com/api/search';</script>
```

Chips toggle engines (multi-select), the tabs switch category, and `/` focuses
the search box.

## Pointing the Android app at it

The Qmamu Search APK opens `https://qmamu.com/search?q=…&engine=…`. Once you host
this, either

- serve it at that path on your domain, or
- change the `QMAMU` constant in `qmamu-search/app/src/main/java/com/qmamu/search/MainActivity.java`
  to your own URL and rebuild.

Because the page reads `?q=` and `?engine=` from the URL, the hand-off works
either way.

## Risks, and how to keep them small

Meta-search means your machine does the fetching, so the usual scraping caveats
apply. For **personal use** these are manageable:

- **CAPTCHAs.** Engines challenge clients that look automated. SearXNG already
  spreads requests, keeps sessions per engine, and lets you disable whichever
  engine starts complaining. If one starts failing, turn it off in
  `searxng/settings.yml` rather than hammering it.
- **IP blocking / rate limits.** The `CACHE_MS` value (default 60 s) means a
  repeated query never leaves your machine. Keep `limiter: false` only while the
  server is private; if you expose it, switch the limiter on and put it behind
  auth.
- **Terms of service.** Scraping public result pages is a grey area and varies by
  engine. Keep it personal and low-volume, identify your client honestly (the
  backend sends a `Qmamu/1.0 (+personal use)` User-Agent), and do not resell or
  republish the results. For anything commercial, use the official APIs.
- **Hosting publicly.** A public instance will get abused and will get your IP
  banned quickly. If you must share it, add authentication, a rate limit, and
  outbound proxying.
- **Freshness.** Aggregated results are only as good as the engines' HTML, which
  changes without notice. Update the SearXNG image regularly — that is where
  those fixes land.

## Tuning

- **Engines** — edit the `ENGINES` map in `server/server.js` (and the matching
  chips in `web/app.js`) to add or drop a source.
- **Cache** — `CACHE_MS` in `docker-compose.yml`.
- **Timeout** — the 12 s `AbortController` in `server/server.js`; raise it if you
  add slow engines.
- **Categories** — the `CATEGORIES` array in `server/server.js` and the tabs in
  `web/app.js`.

## Files

```
qmamu-web/
├── docker-compose.yml     SearXNG + the Qmamu server
├── searxng/settings.yml   enables the json format the backend needs
├── server/
│   ├── server.js          fetch, merge, dedupe, cache, serve  (+ --selftest)
│   ├── package.json
│   └── Dockerfile
└── web/
    ├── index.html         the Qmamu page
    ├── style.css
    ├── app.js
    └── logo.svg
```
