# What you get, and what stays behind

A list of the features in Aekant that are worth carrying to another app, split into
what this kit hands you and what would need work on your side.

## In the kit — drop in and call one function

| # | Feature | What it actually does | Cost to you |
|---|---|---|---|
| 1 | **Ad blocking** | ~133,000 network rules (EasyList, EasyPrivacy, uBlock uAssets, AdGuard Base and Tracking, StevenBlack, 1Hosts, AdGuard CNAME) matched against request hosts, plus ~25,000 cosmetic rules that hide the containers ads live in. Gzipped, loaded once. | `AekantKit.init(context)` + `blockingClient()` |
| 2 | **Tracker blocking** | A separate list of analytics and ad-exchange hosts, switchable independently from ads. | `AekantKit.blockTrackers(true)` |
| 3 | **Per-site allowlist** | Turn blocking off for one host, remembered. | `AekantKit.allow(host, false)` |
| 4 | **Blocking statistics** | A count of what was stopped, persisted. | `AekantKit.blockedCount()` |
| 5 | **Tracker parameter stripping** | ~90 query parameters (`utm_*`, `fbclid`, `gclid`, `msclkid`, `mc_eid`, `igshid`, `trk_*`, …) removed from a URL before it loads. YouTube's `si` is handled separately. | `AekantKit.cleanUrl(url)` |
| 6 | **Background music / video** | A foreground service with a media notification, wake lock, and `mediaPlayback` service type: audio survives backgrounding and screen-off. | `startBackgroundPlayback()` + `setPlaybackActions()` |
| 7 | **Audio keep-alive** | A silent loop that keeps the audio session from being torn down. | `startKeepAlive()` |
| 8 | **Media sniffer** | Every media URL the page exposes, deduplicated and grouped by host — the list behind "download this video". | `mediaFound()` |
| 9 | **Four-part downloads** | Ranged parallel download with pause / resume / cancel and a progress notification, several times faster than one stream. | `canSplit()` then `downloadInParts()` |
| 10 | **Element blocker** | Tap something on the page, keep it hidden for that site forever. | `ElementPicker.start()` + bridge |
| 11 | **Zero dependencies** | Framework only: no AndroidX, no OkHttp, no Kotlin coroutines. Compiles against `minSdk 24`. | nothing |

## Left behind, and why

These are good features, but each one is tied to the browser's own screens, so
they would be a rewrite rather than a copy.

| Feature | Why it is not in the kit |
|---|---|
| **Reader mode** | The extraction heuristics are fine, but the reading screen, typography controls and highlight-to-speak are all app UI. The `READER_JS` extractor alone is ~40 lines and easy to lift by hand. |
| **Read aloud (TTS)** | `TtsReader.kt` is ~400 lines and depends on a sentence splitter, a pronunciation dictionary, a sleep timer and a MediaSession. Worth copying wholesale if you want it, but it is not "small". |
| **Themes and the AMOLED layer** | Built on a theme-attribute system (`?attr/bgColor`, `surfaceColor`, `brandColor`) that every layout in Aekant uses. Useful pattern, useless as a drop-in. |
| **Containers / profiles** | Per-container tabs, cookies and user agents are a browser concept, not a widget. |
| **Extensions and GM_ bridge** | The userscript engine is genuinely reusable, but it needs an injection lifecycle, a store and a UI. Aekant's version is about 900 lines across five files. |
| **Tab switcher, start page, menus** | Pure app UI. |
| **HTTPS-only, WebRTC blocking, canvas/WebGL noise** | These are three short JavaScript snippets (10–30 lines each). They are in Aekant's `MainActivity` rather than the kit because they are one-liners you would paste anyway; ask and they can be lifted out. |

## The honest small print

- The filter lists are the property of their authors (EasyList, AdGuard, uBlock,
  StevenBlack, 1Hosts). They are free to use under their own licences; keep their
  notices if you redistribute.
- Blocking is only as good as the lists. Refresh them periodically — the kit reads
  whatever is in `assets/`.
- The fast downloader writes through MediaStore on Android 10+ and to the app's own
  Downloads folder below that, so it needs no storage permission. If you want the
  files in the public Downloads folder on older versions, add the permission
  yourself.
