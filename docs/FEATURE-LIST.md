# Aekant — complete feature list

Aekant is a full Android browser built on the system WebView, written in Kotlin.
This is the whole of it, taken from the source rather than from memory.

| | |
|---|---|
| Package | `com.vasudev.ekant.browser` |
| Version | 2.0.1007 |
| Engine | Android System WebView |
| minSdk / targetSdk | 24 / 34 |
| Kotlin files | 59 |
| Screens | 33 activities |
| Preference keys | 101 |
| Strings | 671 |

---

## 1. Browsing and tabs

- **Tab switcher** — full-screen, 2-column grid or list, page previews, a search box, an active-tab highlight, and a tab-actions card (new tab, incognito, new group, close all, delete data, settings, about).
- **Tab count badge** on the start page and toolbar; tap to open the switcher.
- **Containers** (profiles) — Personal / Work / Banking / Social by default; each has its own tabs, cookies and storage. Add, rename, delete, switch.
- **Incognito mode** — per-tab, not a global flag. Private tabs keep their own history rule, and the start page switches to a true-black private layout with an information card.
- **Pinned tabs**, **recent tabs**, **reopen closed tab**, **duplicate tab**, **close other tabs**.
- **Restore tabs** on start, per container.
- **Pop-ups as tabs** — `window.open` opens a new tab instead of being dropped.
- **Desktop mode** — per site or globally, with a custom user agent.
- **Homepage** — the built-in start page or any URL you choose.
- **Start page** — Ulaa-style: mode pill, avatar, mute, search pill with mic, 4-column speed dial, Add Site tile, greeting, recently visited.
- **Home shortcuts** — add, edit, reorder, import.
- **Reading list** and **save for later**.
- **Pull to refresh**, **swipe back / forward**, **volume-key scrolling**, **scrollbars** toggle, **compact toolbar**, **toolbar top or bottom**, **address bar top or bottom**, **focus mode** (hides the container row).
- **Kiosk / lock mode** — pin the browser to one site.
- **Immersive mode**, **keep screen on**, **orientation lock**.
- **Vertical reading** and **auto-scroll** with adjustable speed.
- **Auto-retry** on a failed load.
- **Custom schemes** — `aekant://home|tabs|menu|containers|mute|voice|addshortcut|search|open|tools`.
- **External links** — open in the browser or hand off to the system.

## 2. Privacy and blocking

- **Ad filter engine** — EasyList, EasyPrivacy, uBlock uAssets, AdGuard Base and Tracking, StevenBlack, 1Hosts and AdGuard CNAME, shipped gzipped. Roughly **133,000 network rules and 25,000 cosmetic rules**.
- **Per-site allowlist**, plus a quick toggle from the menu.
- **URL filters** — your own regex rules, with a block page.
- **Tracker blocking**, **third-party cookie blocking**, **cookie control**.
- **Safe Browsing** — the engine's own warnings for dangerous sites and downloads.
- **Fingerprint noise** injected into pages.
- **HTTPS-only mode** — upgrades `http://` links.
- **Block app launches** — stops a site opening `intent:`, `market:`, `tel:` and friends.
- **Disable Google AMP** — rewrites AMP links back to the publisher.
- **Clear on exit** and **clear on tab close**, with a choice of what to clear.
- **Reset identity now** — forget cookies, site storage and cache in one tap.
- **Site permissions** — camera, microphone, location, notifications, clipboard and the rest, with a per-site allowlist.
- **Profiles** — separate storage per profile.
- **Network log** — every request the engine makes, viewable in-app.
- **Diagnostics** — engine version, filter counts, last crash.
- **Secure screen** — block screenshots with `FLAG_SECURE`.
- **App lock for private mode** — device authentication before incognito.

## 3. Content tools

- **Reader mode** — article extraction, adjustable text size, page font, highlighted sentences.
- **Read aloud (TTS)** — play, pause, speed, pitch, sentence highlight, a sleep timer with fade, a pronunciation dictionary, battery-aware and live-region options.
- **Translate** — whole page, with a language preference.
- **AI page summary** — summarise the current page (endpoint and model are configurable, key stored locally).
- **Find in page** with a match count.
- **Save as PDF**, **save page offline**, **screenshot the page**.
- **Media on this page** — a sniffer that lists every video and audio file it can find.
- **Block an element** — tap a part of the page; Aekant derives a CSS selector, stores it per site and applies it from then on.
- **User styles** — your own CSS per site.
- **Tools page** — 14 offline utilities including a QR generator.
- **QR / barcode** scanning through the same tools.
- **Share**, **copy**, **page info**, **link long-press menu** (open in new tab, private tab, copy, share, save image).
- **File upload** — `onShowFileChooser` wiring for any site.

## 4. Media and background

- **Media sniffer** and **media list**.
- **Background playback service** — foreground service, wake lock, notification controls.
- **Keep alive** — a silent audio loop to hold the session.
- **Background media** toggle, **YouTube background playback** via an extension.

## 5. Appearance

- **Four themes** — Aekant (orange) and Quiet Nature, each light and dark.
- **AMOLED black interface** — pure `#000000` for the app itself, both brands.
- **Darken websites** — algorithmic dark mode through `androidx.webkit`.
- **Screen filter** — a colour over every page, six tints, adjustable strength, dark-mode only if you want.
- **Text size** and **zoom** presets, **force enable zoom** on pages that block it.
- **Page font** override, **e-ink mode**, **reduce image** handling.
- **Start page background**, **stats** and **top-sites** toggles.

## 6. Accessibility and input

- Text scaling, force zoom, reader and translate shortcuts.
- Volume-key scrolling, swipe navigation, pull to refresh.
- **Back twice to exit**, **last page notification**.
- TalkBack-friendly labels throughout.

## 7. Data and portability

- **Bookmarks**, **history** (with retention: do not save / 1 day / 1 week / 1 month / 6 months / 1 year / unlimited), **downloads**, **reading list**, **files**.
- **Passwords** screen with save and auto sign-in toggles, and import.
- **Autofill** — Aekant's own or the system service.
- **Backup and restore** — export and import everything.
- **Download settings** — save path, ask before downloading, background downloads.
- **Languages** — 22 UI languages.

## 8. Extensions

- **Extensions screen** in add-ons style: coloured icon, name, one-line note, switch, tap to edit or delete.
- **Recommended catalogue** of bundled userscripts (hide YouTube Shorts, skip YouTube ads, stop autoplay, wide GitHub, unstick headers, clean Wikipedia, bigger article text, grayscale images).
- **Extension source** — read `extensions/index.json` from your own GitHub repository and install scripts from it.
- **Install from a link** — paste any `.js` URL.
- **Add manually** and **upload a script** from storage.
- Patterns decide where each script runs (`*`, `*youtube.com*`, comma-separated alternatives).

## 9. Menus and shortcuts

- **Floating overflow card** at the top right: five quick icons (back, forward, bookmark, download, refresh), then one list with thin dividers — no coloured headers.
- **Update Available** row, shown only when a newer build is published.
- Groups: tabs, history and data, library, app, tools.
- **Bottom navigation**: Home / Bookmarks / Downloads / Tabs.

## 10. Power user

- **Remote debugging** over USB (`chrome://inspect`).
- **Custom user agent**.
- **URL filters** and **network log**.
- **Memory saver** with a keep-list.
- **Developer and experimental** switches.
- **Diagnostics** with a crash log.

## 11. Not possible on this engine

Listed so nobody waits for them: Chrome/Firefox extension runtimes (MV2/MV3), Secure DNS / DoH, the Do Not Track header, in-app DevTools panels, true multi-process isolation per tab, torrent downloads, Chromecast, and YouTube video downloading.


## 12. Userscripts that behave like the real thing

- **Metadata parsing** — `@name`, `@description`, `@version`, `@match`, `@include`,
  `@exclude`, `@run-at` and `@grant` are read from the script itself, so a pasted
  or uploaded script arrives with its own name, description and match pattern.
- **`@run-at`** — `document-start` scripts run before the page builds itself;
  everything else runs when the page is ready.
- **GM_ API bridge** — `GM_setValue`, `GM_getValue`, `GM_deleteValue`,
  `GM_listValues`, `GM_addStyle`, `GM_log`, `GM_info` and **`GM_xmlhttpRequest`**.
  Values live in the app's own preferences; the request helper goes through native
  code, so a script is not limited by the page's cross-origin rules.
- The editor has a **Run before the page loads** switch, and the list marks
  scripts that run first or use the GM_ APIs.

## 13. Material You

On Android 12 and later, **Material You colours** take the palette from the
wallpaper. It is a switch in Appearance; restart the app after changing it.


## 14. Fast downloads

- **Four-part downloader** — big files are fetched as four ranged requests at
  once instead of one stream, which is several times quicker on a fast line.
- **Pause and resume** — each part remembers its offset, so a paused download
  carries on from where it stopped; the notification carries Pause, Resume and
  Cancel.
- It only engages when the server offers byte ranges and the file is worth
  splitting; otherwise the system DownloadManager handles it. Files land in the
  public Downloads folder (Android 10+) or the app's Downloads folder below that.

## 15. Per-site script control

- **Scripts on this page** (menu) lists every extension matching the current
  address with a tick or a cross, and switching one off applies to that host
  only - the script keeps working everywhere else.

## 16. Per-container user agent

- Each container can carry its **own user agent**: leave it on Mobile, pin it to
  Desktop, or paste a custom string. Containers sheet → *User agent for this mode*.

## 17. Fingerprint and WebRTC

- Canvas, audio, **WebGL** (vendor, renderer and a pixel of readPixels output),
  hardwareConcurrency and deviceMemory are all perturbed per page.
- **Block WebRTC** (Shields) stops pages learning your local address. Off by
  default, because it can break video calls.

---

## Build

```
compileSdk 34 · minSdk 24 · targetSdk 34
AGP 8.5.2 · Kotlin 1.9.24 · JDK 17
```

The APK is debug-signed, for sideloading and testing.

Aekant also ships a drop-in kit (`aekantkit/`) for the ad-filter engine, the media
service and the keep-alive helper, so the same pieces can be reused in another app.
