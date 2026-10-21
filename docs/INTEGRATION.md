# Reusing Aekant's media, background and ad-block code in another app

This is a practical guide to the pieces of this repository that are worth
reusing, and exactly how to move them into a different Android app.

Everything here is plain Kotlin + AndroidX. No NDK, no native build, no
third-party SDK.

---

## 1. What is inside, and what each part is worth

### 1.1 Video player

There is no ExoPlayer. Video is the WebView's own HTML5 player, which is the
right call for a browser because the site's own player (YouTube, Vimeo, ...)
already handles streaming, captions and quality.

Files: `MainActivity.kt` (WebView setup), `AekantWebView.kt`.

The parts that make it feel like a real player:

| Behaviour | Where | API used |
|---|---|---|
| Full-screen video | `MainActivity` | `WebChromeClient.onShowCustomView` / `onHideCustomView`, view added to `android.R.id.content`; back exits full-screen |
| Pop-up / `target=_blank` opens as a tab | `MainActivity` | `setSupportMultipleWindows(true)` + `WebChromeClient.onCreateWindow` |
| Play without a tap | `newTab()` | `settings.mediaPlaybackRequiresUserGesture = false` |
| Desktop-grade viewport | `newTab()` | `useWideViewPort`, `loadWithOverviewMode`, `setSupportZoom` |
| SSL problems explained | `BrowserClient` | `WebViewClient.onReceivedSslError` |
| Custom schemes (`tel:`, `mailto:`, `upi:`) | `BrowserClient` | `shouldOverrideUrlLoading` |

Manifest: only `INTERNET`.

### 1.2 Music and background audio

Four cooperating pieces:

1. **`MediaPlaybackService.kt`** - a foreground service
   (`foregroundServiceType="mediaPlayback"`) that holds a `PARTIAL_WAKE_LOCK`
   and shows an ongoing notification with **Previous / Play-Pause / Next /
   Stop**. API: `MediaPlaybackService.start(context)`,
   `MediaPlaybackService.stop(context)`, `MediaPlaybackService.running`.
2. **`KeepAlive.kt`** - loops a 1-second near-silent WAV
   (`assets/silent.wav`) at volume `0.001`. This is the "inaudible keep-alive"
   trick: the OS sees active audio and stops throttling the background process.
3. **WebView side** - a tiny JS bridge (`AekantMedia.playing(boolean)`) plus an
   injected listener on `play` / `pause` / `ended`, and one rule in the
   Activity: **do not call `webView.onPause()`** while background playback is
   on, because that is what kills the audio.
4. **`TtsReader.kt`** - read-aloud with a sentence queue, `MediaSession` for
   headset/Bluetooth buttons, audio-focus handling (pauses for calls and other
   audio), sleep timer with fade-out, pitch/rate and a pronunciation
   dictionary.

Also `MediaSniffer.kt` + `MediaActivity.kt`: they collect video/audio URLs seen
in network requests and in the page markup, and offer a download.

Manifest:

```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
...
<service android:name=".MediaPlaybackService"
         android:exported="false"
         android:foregroundServiceType="mediaPlayback" />
```

Honest limits: playback stops if the user swipes the app away (the WebView
lives in the Activity), and DRM/premium content still pauses by the site's own
policy.

### 1.3 Ad and tracker blocking

Two layers:

| Layer | File | What it does |
|---|---|---|
| Host lists | `AdBlocker.kt` + `assets/ads.txt`, `assets/trackers.txt` | small, fast, hand-curated |
| Full filter engine | `AdFilterEngine.kt` + `assets/easylist_net.txt.gz`, `assets/easylist_cosmetic.txt.gz` | EasyList / uBlock / AdGuard syntax: ~133k network rules and ~25k cosmetic rules |

Public API of the engine:

```kotlin
AdFilterEngine.enabled = true
AdFilterEngine.ensureLoaded(context)          // loads on a background thread
AdFilterEngine.isBlocked(url)                 // network blocking
AdFilterEngine.cosmeticCss(pageHost)          // CSS that hides ad containers
AdFilterEngine.ruleCount()                    // for a diagnostics screen
```

Wiring is two hooks in your `WebViewClient`:

```kotlin
override fun shouldInterceptRequest(v: WebView?, r: WebResourceRequest?): WebResourceResponse? {
    val url = r?.url?.toString() ?: return null
    if (AdFilterEngine.isBlocked(url) || adBlocker.isBlocked(url)) {
        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
    }
    return null
}

override fun onPageFinished(v: WebView?, url: String?) {
    val host = Uri.parse(url ?: return).host ?: return
    val css = AdFilterEngine.cosmeticCss(host)
    if (css.isNotEmpty()) {
        v?.evaluateJavascript(
            "try{var s=document.getElementById('adblock');" +
            "if(!s){s=document.createElement('style');s.id='adblock';document.documentElement.appendChild(s);}" +
            "s.textContent=" + org.json.JSONObject.quote(css) + ";}catch(e){}", null)
    }
}
```

`UrlCleaner.kt` is a separate, very cheap win: it strips `utm_*`, `fbclid`,
`gclid`, `igshid` and friends on navigation and before sharing a link.

Limits: no scriptlets (`##+js(...)`), no procedural CSS (`:has-text`), no
`$redirect` / `$csp` / `$removeparam`, no regex rules, and the lists are a
bundled snapshot - there is no auto-update.

---

## 2. Integrating into another app, step by step

### Step 0 - requirements

- Kotlin, `minSdk 24`, `compileSdk 34`, AndroidX (`appcompat`, `core-ktx`,
  `material`).
- `androidx.webkit:webkit:1.11.0` only if you also want the dark-mode and
  WebView-version helpers.

### Step 1 - copy the files

Ad-block (minimum set):

```
AdFilterEngine.kt
AdBlocker.kt
UrlCleaner.kt
assets/ads.txt
assets/trackers.txt
assets/easylist_net.txt.gz
assets/easylist_cosmetic.txt.gz
```

Background audio / video:

```
MediaPlaybackService.kt
KeepAlive.kt
assets/silent.wav
AekantWebView.kt        (optional: selection menu + link long-press)
```

Read-aloud (optional):

```
TtsReader.kt
```

Put them in **your** package and fix the package line and the `R` import in
each file. They only reference each other, plus `MainActivity.PREFS` for the
shared `SharedPreferences` name - replace that with your own constant.

### Step 2 - manifest

Copy the four permissions and the `<service>` block shown in section 1.2.

### Step 3 - assets

Copy the files into `app/src/main/assets/`. Keep the `.gz` names - see the
gotcha in section 3.

### Step 4 - wire the WebView

```kotlin
// once, e.g. in onCreate
AdFilterEngine.enabled = prefs.getBoolean("easylist", true)
AdFilterEngine.ensureLoaded(applicationContext)

// per tab
val w = WebView(this)
w.settings.javaScriptEnabled = true
w.settings.domStorageEnabled = true
w.settings.mediaPlaybackRequiresUserGesture = !prefs.getBoolean("bg_media", false)
w.webViewClient = myClient           // the two hooks from section 1.3
w.addJavascriptInterface(MediaBridge(), "AekantMedia")   // background playback only
```

### Step 5 - background playback

```kotlin
// in the WebViewClient's onPageFinished
w.evaluateJavascript(
    "(function(){if(window.__m){return;}window.__m=1;" +
    "function n(p){try{window.AekantMedia&&window.AekantMedia.playing(p);}catch(e){}}" +
    "['play','playing'].forEach(function(e){document.addEventListener(e,function(){n(true);},true);});" +
    "['pause','ended','emptied'].forEach(function(e){document.addEventListener(e,function(){n(false);},true);});})();",
    null)

// bridge -> activity
private inner class MediaBridge {
    @android.webkit.JavascriptInterface
    fun playing(on: Boolean) { runOnUiThread {
        if (prefs.getBoolean("bg_media", false)) {
            if (on) MediaPlaybackService.start(this@MyActivity)
            else MediaPlaybackService.stop(this@MyActivity)
        }
    } }
}

// and the one line people always miss
override fun onPause() {
    if (!prefs.getBoolean("bg_media", false)) tabs.forEach { it.onPause() }
    super.onPause()
}
```

### Step 6 - check it works

- Open a page with a known ad host: the request should return empty (blocked).
- Open a news site: ad containers should be hidden (cosmetic rules).
- Play a video, press home: audio should continue and the notification should
  appear with four buttons.
- Screen off for a few minutes: audio should keep going (wake lock +
  keep-alive).

---

## 3. Gotchas worth knowing before you copy

1. **`.gz` assets change name at build time.** The Android build expands
   `easylist_net.txt.gz` in `assets/` into a plain `easylist_net.txt` inside
   the APK. `AdFilterEngine` therefore tries the `.gz` name first (wrapped in
   `GZIPInputStream`) and falls back to the plain name. Keep both paths, or you
   will ship an app whose ad-blocker silently loads nothing.
2. **Load the lists off the main thread.** `ensureLoaded()` does this for you;
   133k rules cost roughly 10-15 MB of RAM once parsed. Cap the sets (see
   `MAX_HOSTS`, `MAX_SUBS`) if your app is memory sensitive.
3. **One bad selector voids a CSS rule list.** `cosmeticCss()` chunks the
   selectors, so a single unsupported selector cannot break the whole sheet.
4. **`addJavascriptInterface` is a security boundary.** Keep the bridge tiny
   (a boolean and a string), and never expose files, intents or the account.
5. **WebView cannot run Chrome or Firefox extensions.** That is why this
   repository uses a native filter engine plus userscripts instead of MV2/MV3.
6. **DRM and policy.** Premium/DRM video will still behave the way the site
   wants; background playback is not guaranteed on those streams.
7. **Filter lists go stale.** Re-download them from
   `github.com/easylist/easylist`, `ublockorigin/uAssets`,
   `AdguardTeam/AdguardFilters` and rebuild the assets periodically.

---

## 4. What is not here (and why)

- **Chrome/Firefox extension support** - needs a Chromium or Gecko engine, not
  WebView. This repo keeps a GeckoView spike in `spike-geckoview/` for that
  road.
- **Scriptlet / procedural filter support** - would need a JS injection
  pipeline with per-rule matching; not implemented.
- **Cross-device sync, encrypted database, audio pre-rendering** - each needs
  a backend or a much larger component, so they are deliberately out of scope.
