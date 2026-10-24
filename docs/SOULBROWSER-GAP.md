# Aekant vs SoulBrowser — what can still be added

SoulBrowser's public repository holds no application code, only its translations.
That is still useful: the default `Language/values/strings.xml` has **1039
strings**, and a browser's string table is effectively its feature list. Reading
it end to end gives a complete inventory of what SoulBrowser does, which is what
this note compares against Aekant.

Everything below is judged against one constraint: Aekant renders pages with the
system **Android WebView**. Anything that needs a custom network stack, a real
extension runtime or a media pipeline of its own is out of reach and is listed
as such rather than quietly promised.

---

## 1. Already in Aekant

These came up in the SoulBrowser list and Aekant already has them.

| Area | SoulBrowser | Aekant |
|---|---|---|
| Ad blocking | filter lists, user filters, whitelist | `AdFilterEngine` — ~133k network + ~25k cosmetic rules, per-site allowlist |
| User scripts | UserScript install/list | Extensions screen, plus a GitHub extension source |
| HTTPS | connect with HTTPS only | HTTPS-only mode (Advanced) |
| Popups | popup blocker + whitelist | popups-as-tabs toggle |
| Images | block images | Images toggle (Shields) |
| Remote debugging | web debugging over USB | Remote debugging (Advanced) |
| Downloads | manager, sort, background | download manager, background, ask-before, settings screen |
| Reader | reader mode | Reading mode + reading list |
| TTS | read aloud, speed, tone | Read aloud with sleep timer, pitch, pronunciation |
| Translate | page translation | Translate (WebView + Google endpoint) |
| Find in page | find | Find in page |
| QR | QR/barcode | Tools page (14 utilities incl. QR) |
| Backup | import/export | Backup & restore |
| Themes | light/dark/night | 4 themes + AMOLED true-black interface |
| Incognito | private mode | Incognito mode + private home page, per-tab |
| Media | video manager, background play | Media sniffer + playback service |
| Desktop mode | desktop / mobile | Desktop mode |
| History / bookmarks | lists, folders | History, bookmarks, reading list |
| Memory | memory saving, limits | Memory saver |
| Screenshot / PDF | capture, print | Screenshot, save as PDF, save page |
| Gestures | swipe, volume scroll | Swipe back/forward, volume-key scroll, pull-to-refresh |

---

## 2. Added in this build

Six features that were genuinely missing, each with a switch.

| Feature | Where | What it does |
|---|---|---|
| **Block an element** | Menu → Tools | Tap a part of the page and Aekant derives a CSS selector for it, stores it per site and applies it from then on. The practical way to kill a stubborn banner without writing a filter. |
| **Screen filter** | Settings → Accessibility | A colour laid over every page with adjustable strength, six tints, and an option to apply it only in dark mode. |
| **History retention** | Settings → Browsing → History | Do not save / 1 day / 1 week / 1 month / 6 months / 1 year / unlimited. Older records are dropped on the next start. |
| **Disable Google AMP** | Settings → Advanced | Rewrites `google.<tld>/amp/s/<publisher>` links back to the publisher's own address. |
| **Block app launches** | Settings → Privacy | Stops a site from opening another app on the device (`intent:`, `market:`, `tel:` …) while leaving normal links alone. |
| **Back twice to exit** | Settings → Advanced | On the last page of the last tab, a second press within two seconds closes the app instead of the first one doing it. |

---

## 3. Feasible, not built yet

Ordered by how much they would add per unit of work.

| Feature | Why it is worth it | Notes |
|---|---|---|
| **Block area / element inspection with a saved filter list** | The first version ships; a managed list (edit, per-site view, export) makes it a real filter editor. | Builds directly on what just landed. |
| **Custom web font** | SoulBrowser lets you load a TTF from storage for every page. | Feasible: read the file, embed as a data URI in a `@font-face` rule. Heavy on memory for large fonts. |
| **Video gestures** | Drag the left edge for brightness, the right for volume, sideways to seek. | WebView gives no video API, so it has to be done by injecting a controller into the page and handling the touches there. Works on `<video>` elements the page exposes. |
| **Data saving levels** | Strong / weak, i.e. block images and media on mobile data only. | Straightforward: `blockNetworkImage` plus a per-network rule. |
| **Recent searches** | A list under the search box, with a private-mode option. | Small; the start page already renders lists. |
| **Site theme colour** | Tint the toolbar with the page's `theme-color`. | Read the meta tag after load and set the bar colour. |
| **One-handed mode** | A reachable strip with a duplicate toolbar. | Layout work, no engine dependency. |
| **Tab groups / folders** | Group tabs, auto-group per site, move a tab next to the current one. | Containers exist; grouping inside a container is the next step. |
| **Per-tab network / cookie isolation** | SoulBrowser separates incognito data. | Android 9+ gives WebView a data-directory suffix — partial isolation, not true first-party isolation. |
| **Page source viewer** | Show the raw HTML. | Trivial: read `document.documentElement.outerHTML` into a text view. |
| **Memo / text snippets** | Frequently typed strings to insert. | Small. |
| **Keyboard security** | Stop the IME from learning what you type. | One IME flag on the address bar. |
| **Overlay blocker modes** | Block all / only suspected ads, with a whitelist. | Our cosmetic filter already removes overlays; the modes are UI. |

---

## 4. Not possible on this engine

Stated plainly so nobody waits for them.

| Feature | Why not |
|---|---|
| **Chrome/Firefox extension support (MV2/MV3)** | Needs a Chromium or Gecko engine with an extension runtime. WebView has none. Our Extensions are userscripts injected into matching pages. |
| **Torrent download** | Needs a BitTorrent client in the app. Also not something a browser should quietly do. |
| **Chromecast / TV cast** | Needs the Cast SDK and a media route provider; the WebView's `<video>` is not exposed for casting. |
| **Secure DNS / DoH** | No public WebView API to set a resolver. |
| **Do Not Track header** | WebView cannot attach custom headers to the requests it makes. |
| **Element inspection with the DevTools protocol in-app** | Only over USB, via `chrome://inspect` (we expose that toggle). |
| **True multi-process isolation per tab** | One WebView process model; the engine decides. |
| **Downloading YouTube video** | Against YouTube's terms, and the streams are not plain files. |
| **OCR image translation** | Possible only with a bundled OCR model — a large download for a narrow case. Not planned. |

---

## 5. What this build changed in code

New files:

- `ElementBlocks.kt` — per-site storage of picked selectors
- `ScreenFilterActivity.kt` — colour, strength, dark-mode-only
- `HistorySettingsActivity.kt` — retention options

Touched:

- `MainActivity.kt` — element picker + `AekantBlock` bridge, screen-filter layer,
  AMP rewrite, app-launch guard, double-back, `pruneHistory`
- `ShieldsActivity.kt`, `AdvancedActivity.kt`, `SettingsActivity.kt` — the new switches and rows
- `activity_main.xml` — the filter overlay
- `AndroidManifest.xml`, `strings.xml`
