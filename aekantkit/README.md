# AekantKit - ad-blocking + background audio, in three calls

A self-contained drop-in for any Android app that shows web content in a WebView.
No NDK, no third-party SDK, no Gradle module to wire up.

## What you get

- **Ad and tracker blocking** - EasyList / uBlock / AdGuard filter syntax, ~133k
  network rules plus ~25k cosmetic rules, bundled offline.
- **Cosmetic filtering** - hides ad containers with injected CSS (chunked, so one
  bad selector cannot break the whole sheet).
- **Link cleaning** - strips `utm_*`, `fbclid`, `gclid`, `igshid` and friends.
- **Background audio** - a foreground service with a wake lock plus a
  near-silent keep-alive loop, so audio keeps playing with the screen off.

## Install in 3 steps

### 1. Copy two folders

```
aekantkit/*.kt        ->  app/src/main/java/com/aekant/kit/
aekantkit/assets/*    ->  app/src/main/assets/
```

(Keep the `.gz` file names exactly as they are.)

### 2. Add the manifest bits

See `manifest-snippet.xml` - four permissions and one `<service>` element.

### 3. Call it

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AekantKit.install(this)                                   // load the lists once

        val web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webViewClient = AekantKit.client(this, myOwnClient)   // ad-block + cosmetic
        AekantKit.enableBackgroundAudio(web, this)                // optional
        web.loadUrl("https://example.com")
    }

    override fun onPause() {
        // Do NOT call web.onPause() if you want background audio to continue.
        super.onPause()
    }
}
```

`myOwnClient` can be your existing `WebViewClient` or `null`. AekantKit wraps it
and forwards every other callback to it.

## API

| Call | What it does |
|---|---|
| `AekantKit.install(context)` | loads the filter lists on a background thread; call once |
| `AekantKit.setAdBlocking(on)` | turn filtering on/off at runtime |
| `AekantKit.client(context, base)` | a `WebViewClient` with blocking + cosmetic CSS |
| `AekantKit.enableBackgroundAudio(webView, context)` | keep audio alive in the background |
| `AekantKit.stopBackgroundAudio(context)` | stop the service and the keep-alive loop |
| `AekantKit.cleanUrl(url)` | strip tracking parameters from a link |
| `AekantKit.ruleCount()` | how many network rules are loaded |

## Requirements

- Kotlin, `minSdk 24`, `compileSdk 34`, AndroidX.
- Nothing else. `androidx.webkit` is only needed if you also want the engine's
  algorithmic dark mode, which is not part of this kit.

## Notes

- The lists cost roughly 10-15 MB of RAM once parsed; they load off the main
  thread.
- The Android build expands `.gz` files in `assets/` and drops the extension, so
  the engine tries the `.gz` name first and falls back to the plain name.
- WebView cannot run Chrome or Firefox extensions; this kit is the native
  equivalent for blocking, and userscripts cover the rest.
- Rebuild the two list files periodically from `easylist/easylist`,
  `ublockorigin/uAssets` and `AdguardTeam/AdguardFilters`.
