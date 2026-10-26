# AekantKit

The reusable half of the Aekant browser, in about 2,000 lines of Kotlin with **no
dependencies**. Drop the folder into any Android app and you get ad blocking,
background media, a fast downloader and a few smaller tools.

## Install

1. Copy the `aekantkit` folder into your project, for example
   `app/src/main/java/com/aekantkit/`, and the `assets/` files into
   `app/src/main/assets/`.
2. Add the service from `manifest-snippet.xml` to your `AndroidManifest.xml`.
3. Nothing else. `build.gradle-snippet.txt` explains why: there is nothing to add.

Requires `minSdk 24` and Kotlin 1.9+. Works with Java too, since every entry point
is a plain object or class.

## The two calls that matter

```kotlin
// Application.onCreate()
AekantKit.init(this)

// for each WebView you create
webView.webViewClient = AekantKit.blockingClient(yourOwnClient)
```

`blockingClient` drops requests that match the filter lists and hands everything
else to your own `WebViewClient`, so your page callbacks keep working.

## What is in the box

| Module | File | What it gives you |
|---|---|---|
| **Ad + tracker blocking** | `AdFilterEngine.kt`, `AdBlocker.kt` | ~133,000 network rules and ~25,000 cosmetic rules from EasyList, EasyPrivacy, uBlock, AdGuard, StevenBlack and 1Hosts, shipped gzipped. Host matching, per-site allowlist, blocking stats. |
| **Cosmetic hiding** | `AekantKit.applyCosmetic(webView, host)` | The CSS that removes ad containers, injected into the page. |
| **Tracker stripping** | `UrlCleaner.kt` | Removes `utm_*`, `fbclid`, `gclid`, `mc_eid`, `igshid` and ~90 more from any URL before it loads. |
| **Background music / video** | `MediaPlaybackService.kt` | A foreground service with a media notification (previous / play-pause / next / stop), a wake lock and the right `foregroundServiceType`, so audio keeps playing when your app is in the background or the screen is off. |
| **Audio session keep-alive** | `KeepAlive.kt` | Holds the audio session open with a bundled silent loop. |
| **Media sniffer** | `MediaSniffer.kt` | Collects every media URL a page reveals (video, audio, HLS), deduplicated, per host. |
| **Fast downloader** | `FastDownloader.kt` | Splits a file into four ranged requests, runs them in parallel, with pause, resume, cancel and a progress notification. Falls back cleanly when the server does not support ranges. |
| **Element blocker** | `ElementPicker.kt`, `ElementBlocks.kt` | Tap an element on the page, get a CSS selector, store it per host, hide it from then on. |

## Wiring the media bits

```kotlin
// keep audio alive while the page plays
AekantKit.startBackgroundPlayback(this)
AekantKit.setPlaybackActions(object : MediaPlaybackService.Actions {
    override fun toggle() { myPlayer.pauseOrResume() }
    override fun next()   { myPlayer.next() }
    override fun prev()   { myPlayer.previous() }
})

// element blocker: register the bridge, start the picker, re-apply on load
webView.addJavascriptInterface(ElementPicker.PickerBridge(this), "AekantBlock")
ElementPicker.start(webView)                      // when the user taps "block an element"
ElementPicker.apply(webView, this, host)          // on every page load

// downloads
val total = AekantKit.canSplit(url)               // -1 means: use DownloadManager
if (total > 0) AekantKit.downloadInParts(this, url, name, mime, null, total)
```

## Notes

- The filter lists live in `assets/` and are read once on first use. Android's
  build step expands `.gz` files, so the engine tries the compressed name first
  and then the plain one — keep both paths working if you rename them.
- Nothing here phones home, needs an API key, or includes analytics.
- `FEATURES.md` lists what was deliberately left out of the kit, and why.
