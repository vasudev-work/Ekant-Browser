# Qmamu (app)

The Qmamu meta-search frontend, packaged as an Android app.

The whole interface is bundled in `app/src/main/assets/www`, so it opens
instantly and works even if a site is down — only the results come from the
network. The page talks to the **Qmamu backend** from `qmamu-web/`.

## How it fits together

```
Qmamu app  ──►  bundled page (assets/www)  ──►  Qmamu backend  ──►  SearXNG  ──►  engines
```

- `MainActivity` serves the page through `WebViewAssetLoader`, so it runs on a
  normal `https://appassets.androidplatform.net` origin instead of `file://`.
- `index.html` is rewritten in `shouldInterceptRequest` to carry
  `window.QMAMU_API = "<backend>"` — the page knows where to look before any of
  its own scripts run.
- Links to the wider web leave the app; Qmamu's own pages stay inside.

## Setting the backend

Two ways, both fine:

1. **In the app** — tap the gear button next to the search box, paste your
   backend address (`https://your-host/api/search`) and Save. It is kept in the
   page's local storage and wins over the built-in default.
2. **As the built-in default** — change `DEFAULT_API` in
   `app/src/main/java/com/qmamu/app/MainActivity.java` and rebuild.

The default is `https://qmamu.com/api/search`, so if you serve the backend on
your own domain under `/api/search` the app works with no configuration at all.

## Also a search provider

The activity answers `ACTION_WEB_SEARCH`, so other apps (and the system search
UI) can hand Qmamu a query directly — it opens with the results already running.

## Build

```bash
cd qmamu-app
gradle assembleDebug
# app/build/outputs/apk/debug/app-debug.apk
```

CI builds it on every push that touches this folder and attaches the APK to the
`qmamu-app-latest` release:

```
https://github.com/vasudev-work/Ekant-Browser/releases/download/qmamu-app-latest/Qmamu-debug.apk
```

## Notes

- `usesCleartextTraffic` is on so you can point the app at a backend on your own
  LAN (for example `http://192.168.1.10:8080/api/search`) while testing. Turn it
  off once your backend is on HTTPS.
- Debug-signed, for sideloading and testing.
