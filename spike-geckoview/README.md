# Aekant GeckoView spike (non-Chromium engine)

A **separate, minimal Gradle project** that swaps Android's Chromium-based `WebView`
for **Mozilla GeckoView** (the Firefox engine). It is a proof-of-concept, not the
main app. The main app lives in the repo root and still uses WebView.

## What this spike proves

- Runs a **non-Chromium engine** on Android (Gecko, via `GeckoRuntime` + `GeckoSession`).
- **Per-container isolation that is real**: each container is a `GeckoSession` created
  with its own `GeckoSessionSettings.contextId(...)`, so cookies/cache/storage do not
  leak between containers. (On WebView we could only fake this.)
- **Top-level ad/tracker blocking** through `NavigationDelegate.onLoadRequest`
  returning `AllowOrDeny.DENY`.
- Basic tabs, URL/search bar, back/forward/reload/home, title and progress.

## Build

This project needs `maven.mozilla.org` (declared in `settings.gradle.kts`). It is built
by `.github/workflows/build-geckoview-spike.yml`, which resolves the latest GeckoView
release from Mozilla's Maven metadata and passes it as `GECKOVIEW_VERSION`.

```bash
gradle -p spike-geckoview assembleDebug
```

Note: `geckoview-omni` bundles every ABI, so the APK is large (tens of MB). For a real
release you would ship per-ABI artifacts (`geckoview-arm64-v8a`, `geckoview-x86_64`, ...).

## What is still missing here (deliberately)

- **Subresource ad-blocking** (ads/trackers inside a page) needs a WebExtension-style
  blocker, not `onLoadRequest` (that only sees top-level navigations). GeckoView can load
  a built-in uBlock-Origin-style extension with `GeckoRuntime.registerWebExtension`.
- **Real MV2/MV3 extensions** — GeckoView supports WebExtensions, so this is the path to
  actual Chrome/Firefox-style extension support that WebView cannot give you.
- **DNS-over-HTTPS** — configure through Gecko runtime preferences.
- **Background media playback**, containers UI, tab grid, bookmarks/history DB — not ported
  yet; the WebView app has these and they would be re-plumbed onto GeckoSession.
- **APK size / Play policy** — GeckoView adds a lot of size; review before shipping.
