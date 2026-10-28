# Aekant — Android (merged build)

This project merges two codebases into one Android app:

- **Aekant** — the Kotlin, brand-first Android client (dark UI, orange→green gradient,
  cup logo, containers, ad/tracker blocker).
- **My Browser** — the feature-complete WebView browser (tabs, incognito, sliding menu,
  new-tab page, history/bookmarks/downloads, find, zoom, share, translate, desktop mode,
  the full settings hub, shields, passwords/autofill, AI assistant, About).

**Base = Aekant (Kotlin).** Everything from My Browser has been ported into it, so the
result is one app: Aekant's identity and privacy engine with My Browser's feature set.

- **Application ID / namespace:** `com.vasudev.ekant.browser`
- **Display name:** Aekant
- **Version:** 2.0

> Aekant (https://aekant.com) is India's privacy-first, non-Chromium sovereign browser.
> The desktop builds ship for Windows/macOS/Linux. This is an independent Android client
> that mirrors Aekant's ideas on Android; it is not the official Aekant app.
> The browser feature set is ported from **My Browser** by Vasudev Dangaya.

## Download & install (Android)

**Latest APK (built by CI on every push):**

    https://github.com/vasudev-work/Ekant-Browser/releases/latest/download/Aekant-debug.apk

Install it on a phone with USB debugging on:

    adb install Aekant-debug.apk

Or copy `Aekant-debug.apk` to the phone, open it and allow "install unknown apps".

> This is a debug-signed build for sideloading and testing. To publish on the Play Store
> you need your own release keystore — never commit that keystore (it is already ignored
> in `.gitignore`).

### Build from source

    git clone https://github.com/vasudev-work/Ekant-Browser
    cd Ekant-Browser
    gradle assembleDebug          # or just open the folder in Android Studio

Requirements: JDK 17 and Android SDK 34 (compileSdk 34, minSdk 24).

## What's in the merged app

**Aekant side**
- Dark-first brand theme (orange `#F26A1B` / green `#8CC63F`), cup-with-steam logo.
- **Containers** — Personal / Work / Banking / Social workspaces, each with its own tabs,
  persisted locally. Create, switch and delete from the menu panel.
- **Ad blocker** and **tracker protection** — bundled host lists (`assets/ads.txt`,
  `assets/trackers.txt`) answered with an empty response in `shouldInterceptRequest`.
- **Third-party cookie blocking** via `CookieManager`.
- Branded **new-tab page** (`assets/ntp.html`): gradient wordmark, private search box,
  top sites and stats, driven by the `aekant://` scheme.

**My Browser side (ported to Kotlin)**
- Multi-tab browsing with a **sliding bottom menu panel** (quick actions + sections:
  Tabs · Library · Tools · More).
- **Incognito tabs** with optional **biometric lock**.
- **New-tab page** options: background, top sites, stats, opening-screen behaviour.
- **History, bookmarks and downloads** (via the system Download Manager) shown in animated
  **bottom sheets**.
- **Find in page, zoom, share, translate, desktop-site mode.**
- Full **Settings hub**: General (search engine, homepage, NTP, downloads, close-on-last),
  Display (appearance, languages), Assistant, Passwords & autofill, Privacy & security, About.
- **Shields**: block ads, trackers, third-party cookies, scripts, cookies, images.
- **AI assistant** — chat with any OpenAI-compatible endpoint (endpoint, key and model are
  user-configurable; no keys are hardcoded).
- **Appearance** (dark/light + text size), **Languages**, **Password manager**,
  **Autofill services**, **About** (UPI support + feedback).

## Project structure

```
Aekant/
├── settings.gradle · build.gradle · gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle                      # namespace / applicationId = com.vasudev.ekant.browser
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/  ads.txt · trackers.txt · ntp.html
        ├── java/com/vasudev/ekant/browser/
        │   ├── MainActivity.kt          # tabs, containers, panel, sheets, ad-block
        │   ├── Sheet.kt                 # animated bottom sheet
        │   ├── SettingsBuilder.kt       # card/row builder
        │   ├── BaseSettingsActivity.kt  # settings scaffold
        │   ├── SettingsActivity.kt · ShieldsActivity.kt · AppearanceActivity.kt
        │   ├── NewTabPageActivity.kt · LanguagesActivity.kt
        │   ├── PasswordsActivity.kt · AutofillActivity.kt
        │   ├── AIActivity.kt · AboutActivity.kt
        │   ├── AdBlocker.kt · Container.kt · ContainerStore.kt
        └── res/  (drawables, layouts, values)
```

## Build

1. Open the `Aekant` folder in **Android Studio** (Giraffe/Hedgehog or newer).
2. Let Gradle sync — compileSdk 34, minSdk 24, AGP 8.5.2, Kotlin 1.9.24.
3. Run on a device or emulator (API 24+).

The binary `gradle-wrapper.jar` is not bundled; Android Studio generates the wrapper on
first open, or run `gradle wrapper` once from the project root.

## Not included (and why)

- **Per-container VPN tunnel** — Aekant's flagship desktop feature. It needs a real
  `VpnService` plus VPN servers, which cannot ship inside a code-only project.
- **Chrome extensions** — the app renders with the system WebView engine, so it cannot run
  extensions (the in-app Extensions sheet explains this, as in My Browser).
- **Container storage isolation** is UI/tab-level on Android: the platform keeps a single
  `CookieManager` per process. True sandboxing would need one Android process per container
  (`android:process`).
