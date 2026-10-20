# Aekant for Android — status against the master specification

This maps the 32-section "next-generation Android web browser" master prompt to what
this repository actually ships today. Honest labels only: **Done**, **Partial**, **Planned**.

| # | Spec section | Status | Notes |
|---|--------------|--------|-------|
| 1 | Role and objective | — | Real browser, every control performs a real action. |
| 2 | Technology stack | Partial | Kotlin + Material 3 + AndroidX. **XML layouts, not Compose**; no Hilt/Room/DataStore yet. Engine is behind a subclass (`AekantWebView`) and a separate GeckoView project exists. |
| 3 | Design system and branding | Partial | Light/Dark/Follow-system, two brands (Aekant, Quiet Nature), reduced clutter. Custom accent colour and reduced-motion are planned. |
| 4 | Main browser screen | Done | Address bar, back/forward/reload, progress, title, bottom navigation, menu panel. |
| 5 | New tab and home dashboard | Partial | Search bar, editable shortcuts, recently visited, greeting, privacy summary, start-page styles (Minimal/Personal/Dashboard). Reading list and notes/weather widgets planned. |
| 6 | Tab management | Done | Multiple tabs, grid switcher with search, reopen closed, duplicate, pin, close all, containers. Real tab groups planned. |
| 7 | Bookmarks and reading list | Partial | Bookmarks + **reading list** (save/mark read/remove). Bookmark folders, search, import/export planned. |
| 8 | Browsing history | Partial | List, delete, clear all. Search, group-by-date and time-range clearing planned. |
| 9 | Download manager | Partial | System DownloadManager + downloads screen. Pause/resume planned. |
| 10 | Website compatibility | Done | JavaScript, cookies, images, pop-ups, zoom, desktop site, PDF handling. |
| 11 | Private browsing | Partial | Incognito mode and a separate-private option. True storage isolation is not possible with WebView. |
| 12 | Privacy dashboard | Done | Protection state + data controls. |
| 13 | Ad and tracker blocking | Partial | Bundled host lists. EasyList syntax + cosmetic filtering planned. |
| 14 | Site permissions | Done | Global block plus a per-site allow list, wired to `onPermissionRequest`. |
| 15 | Search engine management | Done | Add / edit / delete / set default; Google, Bing, DuckDuckGo, Brave, Ecosia, Qmuma, iBharat, Indus AI, Sarvam AI. |
| 16 | Advanced settings | Done | Full grouped settings hub (General/Browsing/Privacy/Performance/Accessibility/Advanced/About). |
| 17 | Context menu and text actions | Done | Selection menu: Search / Share / Translate / Ask AI. Long-press link: open in new/private tab, copy, share; image: save. |
| 18 | Reader mode | Partial | Basic reader extraction. Font size, line spacing, sepia, progress, TTS planned. |
| 19 | Translation and language | Partial | Page translation from the menu; 22-language list. Full in-page translation planned. |
| 20 | AI browser assistant | Done | Chat, page summary, and Ask-AI on selected text. |
| 21 | Sync and backup | Partial | Local JSON backup/restore. Cross-device sync needs a backend. |
| 22 | Password and autofill | Partial | Android Autofill + the optional Vasu settings module. Own encrypted manager planned. |
| 23 | Extensions and plugins | Partial | Userscript-style extensions (add/upload). Real MV2/MV3 needs a Gecko engine. |
| 24 | Performance and resource management | Done | Memory saver, data saver, background media, cache control. |
| 25 | Security architecture | Partial | HTTPS-only, fingerprint noise, permission blocking, no telemetry. Keystore-backed secrets planned. |
| 26 | Accessibility and usability | Partial | Text size/zoom, large touch targets. Screen-reader and large-font pass planned. |
| 27 | Offline and error handling | Done | Branded error page; save page offline (HTML). |
| 28 | Database design | Planned | Storage is SharedPreferences today; a Room migration is planned. |
| 29 | Project structure | Partial | Single module; the layered MVVM structure is planned. |
| 30 | Testing requirements | Planned | No automated tests yet. |
| 31 | Build and release | Done | CI builds a debug APK on every push and publishes it to the `latest` release. |
| 32 | Implementation workflow | Partial | Incremental feature work on top of a working app, as the spec asks. |

## Engine note

Desktop Aekant is a Gecko (Firefox) build. Android's system WebView cannot run Chrome
extensions or provide true per-tab process isolation, so `spike-geckoview/` explores
Mozilla GeckoView as the engine for a future non-Chromium build.

## Roadmap (next)

1. EasyList-compatible ad-blocking engine (cosmetic filtering + scriptlets).
2. Room + DataStore migration for history/bookmarks/reading list.
3. Reader mode upgrades (font, spacing, sepia, progress, TTS).
4. History search, grouping and time-range clearing.
5. Automated tests and a signed release pipeline.
