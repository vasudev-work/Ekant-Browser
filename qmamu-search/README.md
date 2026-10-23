# Qmamu Search

A single-screen search utility for Android. It opens the Qmamu search engine at
`https://qmamu.com/search` in a full-screen WebView and keeps every link inside
the app.

Written in **Java + XML** (no Kotlin, no Compose), `minSdk 21`, `targetSdk 34`.

## What it does

| # | Requirement | Where |
|---|---|---|
| 1 | Full-screen WebView, no action bar | `activity_main.xml`, `themes.xml` |
| 2 | JavaScript, DOM storage, zoom, wide viewport | `MainActivity.onCreate` |
| 3 | Links stay in the app | `WebViewClient.shouldOverrideUrlLoading` |
| 4 | Back walks history, then exits | `MainActivity.onBackPressed` |
| 5 | Pull to refresh | `SwipeRefreshLayout` + `onPageFinished` |
| 6 | Offline screen with Retry | `isOnline()` / `loadIfOnline()` / `onReceivedError` |

## Search engines

A chip row sits above the page with six engines: **Qmamu, Google, Bing,
DuckDuckGo, Brave and Yandex**. Pick one, type a query and press Go (or the
keyboard's search key).

Whatever you pick, the results always open on the **Qmamu search page**:

```
https://qmamu.com/search?q=<your query>&engine=<chosen engine id>
```

The `engine` parameter carries your choice so qmamu.com can use it. Typing
something that looks like a host name (e.g. `example.com`) opens that site
directly instead of searching.

## Speed

The app is built to show the search page as quickly as possible:

- **The load starts in `onCreate`**, without waiting for a connectivity probe. The
  offline screen only appears if the page really fails (`onReceivedError`).
- **Cache-first first paint.** The initial load uses
  `LOAD_CACHE_ELSE_NETWORK`, so a repeat launch paints the cached page instantly;
  as soon as it is on screen the mode flips to `LOAD_DEFAULT` so every search is
  current.
- **DNS warm-up.** The host name is resolved on a low-priority background thread
  while the WebView engine boots.
- **No WebView rebuild.** `configChanges` keeps the instance across rotation, and
  hardware acceleration is on.

## Build

```bash
cd qmamu-search
gradle assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

CI builds it on every push that touches this folder and attaches the APK to the
`qmamu-latest` release:

```
https://github.com/vasudev-work/Ekant-Browser/releases/download/qmamu-latest/QmamuSearch-debug.apk
```

## Look and feel

It borrows the Aekant palette so the two apps feel like a family: dark background
`#0B0E12`, brand orange `#FF6600`, and the same rounded launcher icon, with a
`Q` in place of the Aekant mark.

## Notes

- Debug-signed, for sideloading and testing.
- The search host is a single constant (`HOME` in `MainActivity`) so it is easy
  to point at a different endpoint.
