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
