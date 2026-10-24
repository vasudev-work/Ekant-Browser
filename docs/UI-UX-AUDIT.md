# Aekant — UI/UX and QA audit

Reviewed as a designer and a bug hunter, against the source rather than
screenshots: every icon, corner radius, text size and touch target in the app was
counted from the layouts and the Kotlin that builds the screens. Where a number is
quoted below, it is the number in the code.

Scope: 31 vector icons, 4 layout files, 33 screens (most built programmatically),
671 strings, 101 preference keys.

---

## 1. Icons

### What is already right

- **One visual family.** Every icon is a 24dp vector in stroke (line-art) style
  with a 24-unit viewport. Nothing is half-filled, half-flat, so the set reads as
  one family. That is the hard part and it is done.
- **The quick-action row is clean.** Back, forward, bookmark, download and
  refresh each have their own glyph, one meaning each.

### Problems

**1.1 Stroke weight is mixed.** The same set is drawn at four different weights:

| Weight | Icons |
|---|---|
| 1.6 | `ic_ai`, `ic_ext` |
| 1.7 | `ic_bookmark`, `ic_settings`, `ic_find`, `ic_share`, `ic_star`, `ic_lock`, `ic_exit` |
| 1.8 | `ic_tabs`, `ic_info` |
| 2.0 | `ic_home`, `ic_download`, `ic_back`, `ic_chev` |

At 22–24dp on a phone this reads as "slightly bolder, slightly thinner" from row
to row. Pick **one** weight — 1.75 sits between the two groups and matches
Material Symbols at 24dp — and redraw the outliers.

**1.2 One icon breaks the family.** `ic_info` has a solid white dot inside a
stroke circle. Every other icon is pure outline.

**1.3 The Settings screen has no icons at all.** This is the biggest gap. The
settings builder is called 183 times with a text-only row, and only 3 rows
anywhere in the app carry an icon:

```
b.navRow(card, getString(R.string.s_appearance), null) { ... }   // no icon
b.navRow(card, getString(R.string.s_homepage), null) { ... }     // no icon
```

So every screen under Settings — General, Browsing, Privacy, Performance,
Accessibility, Advanced, About and their sub-screens — is a plain list of words.
A leading icon per row is what makes a settings list scannable.

**1.4 Several icons mean more than one thing.** Counted from the menu builder:

| Icon | Used for | Reads as |
|---|---|---|
| `ic_download` | Downloads, Save as PDF, Media on this page, Save a picture of the page, quick-download | 5 different actions |
| `ic_ext` | Add tab to new group, Scripts on this page, Tools, Extensions | 4 |
| `ic_star` | Bookmarks, Save for later, Pin to Home screen | 3 |
| `ic_ai` | AI page summary, Read aloud, AI assistant | 3 |
| `ic_folder` | Save page offline, Files, Upload a script | 3 |
| `ic_find` | Find in page, Reader mode | 2 |
| `ic_info` | Update available, Help and feedback | 2 |
| `ic_lock` | Block an element, Lock the browser (kiosk) | 2 |
| `ic_exit` | Delete browsing data, Exit | 2 |

A star that sometimes means "pin" and a padlock that sometimes means "block this
element" is how users mis-tap.

### Recommended mapping (Material Symbols Outlined, 24dp, weight 1.75)

| Row | Use |
|---|---|
| Appearance | `palette` |
| Homepage | `home` |
| Search engine | `travel_explore` |
| Toolbar | `toolbar` |
| Tabs | `tab` |
| Bookmarks | `bookmark` |
| History | `history` |
| Reading list | `menu_book` |
| Files | `folder_open` |
| Downloads | `download` |
| Download settings | `tune` |
| Website settings | `language` |
| Reading mode | `chrome_reader_mode` |
| Privacy dashboard | `shield` |
| Ad blocking | `block` |
| Tracking | `visibility_off` |
| Cookies | `cookie` |
| Permissions | `verified_user` |
| Profiles | `account_circle` |
| Passwords | `key` |
| Autofill | `edit_note` |
| Clear data | `delete_sweep` |
| Data usage | `data_usage` |
| Cache | `cached` |
| Background | `memory` |
| Text size | `format_size` |
| Screen filter | `brightness_6` |
| Accessibility | `accessibility_new` |
| Backup | `cloud_upload` |
| Developer | `code` |
| Diagnostics | `monitor_heart` |
| Network log | `list_alt` |
| URL filters | `filter_alt` |
| Extensions | `extension` |
| Screen filter colour | `water_drop` |
| History retention | `schedule` |
| Update available | `system_update` |
| Help and feedback | `help` |
| About | `info` |
| Block an element | `select_all` (not a padlock) |
| Save as PDF | `picture_as_pdf` |
| Save page offline | `save` |
| Media on this page | `movie` |
| Screenshot | `photo_camera` |
| Save for later | `bookmark_add` |
| Pin to Home screen | `push_pin` |
| Read aloud | `record_voice_over` |
| Tools | `handyman` |
| Scripts on this page | `code_blocks` |
| Delete browsing data | `delete` (not the exit arrow) |
| Exit | `logout` |

---

## 2. Panels, modals and hierarchy

**2.1 Corner radii are not on a scale.** Eight different radii are in use for
shapes that should belong to one system:

| Radius | Count | Used for |
|---|---|---|
| 10dp | 1 | an icon tile |
| 11dp | 1 | an icon tile |
| 12dp | 1 | a button |
| 14dp | 1 | a tile |
| 16dp | 3 | cards |
| 18dp | 1 | a chip |
| 20dp | 2 | the menu card, a panel |
| 22dp | 1 | a search field |

Ten and eleven differ by a pixel nobody can see, but the eye reads the mismatch
between 12 and 22. Use four tokens: **12** (small chips), **16** (cards, rows),
**20** (sheets and the menu card), **28** (search fields and pills).

**2.2 Eleven text sizes for a five-level hierarchy.** Sizes in the source:
10, 11, 12 (×7), 13 (×5), 14 (×4), 15 (×5), 16 (×3), 18 (×2), 22, 24, 26.
12/13 and 14/15 are visually the same and are used interchangeably. A workable
scale: **11 caption · 13 secondary · 15 body · 18 title · 22 headline**.

**2.3 Contrast is good, with one exception.** Measured against the dark surface:

| Pair | Ratio | Verdict |
|---|---|---|
| `text_primary` on `surface` | ≈ 15:1 | excellent |
| `text_secondary` on `surface` | ≈ 6.7:1 | passes AA |
| `brand_orange` on `bg` | ≈ 5.8:1 | passes AA |
| `brand_orange` on `surface2` | ≈ 5.3:1 | passes AA |
| white on `brand_orange` | ≈ 3.0:1 | **only passes for bold ≥ 19px** |

The white-on-orange pairing is used on filled buttons. Keep it only for bold
labels 15sp and up; for anything smaller put dark text on the orange. The tab
switcher's `+` button already does the right thing — it sets the glyph to the
background colour on the brand fill.

**2.4 The menu card is a fixed width.** `panel` is `310dp` in the layout and the
same number is set again in code. On a 320dp-wide device that leaves a 10dp
margin; on a tablet it is a narrow strip with a lot of empty space beside it.
Use `min(320dp, screenWidth − 24dp)`.

**2.5 The bottom bar has no selected state.** All four items — Home, Bookmarks,
Downloads, Tabs — draw their icon in `textPrimaryColor` and their label in
`textSecondaryColor`, always. There is no way to see which section you are in.
Tint the active icon and label with the brand colour.

**2.6 The bottom bar labels are 10sp.** That is under the 12sp minimum for
readable captions on a phone.

**2.7 Panels do not agree with each other.** The menu card uses `menu_card_bg`
(20dp, with a hairline stroke), the sheet uses `panel_bg_dark`/`panel_bg_light`,
and dialogs use the platform default. Three different surfaces for the same kind
of object.

---

## 3. Bugs and usability

**3.1 Touch targets under 48dp.** Measured from the layout parameters:

| Element | Size | |
|---|---|---|
| Tab counter chip | 44 × 36dp | ✗ too short |
| Avatar / mute circles (start page) | 44 × 44dp | ✗ |
| Quick actions in the menu | ≈ 24 + 7 + 14 = 45dp | ✗ borderline |
| Engine chips | ≈ 36dp tall | ✗ |
| `+` new tab button | 48 × 48dp | ✓ |
| Settings rows | ≈ 15 + 20 + 15 = 50dp | ✓ |
| Bottom bar items | 58dp tall | ✓ |

Anything under 48dp is a mis-tap waiting to happen; the counter chip at 36dp is
the worst of them because it sits next to the overflow button.

**3.2 Dead resources after the menu rework.** The menu used to be a bottom sheet
with a drag handle; it is now a floating card. `handle_bg`, `panel_bg_dark` and
`panel_bg_light` are now referenced only by the sheet (and `handle_bg` by
nothing). Either reuse them for the sheet or delete them.

**3.3 Long labels have nowhere to go.** The menu card is 310dp wide and rows are
single-line. Labels such as *Save a picture of the page* and *Delete browsing
data* are close to the limit and will clip with a longer translation. Set
`maxLines = 1` with `ellipsize = END` (or allow two lines).

**3.4 Settings rows mix two conventions.** Settings rows carry a chevron;
menu rows deliberately do not. That is defensible, but the *sub-screens* that
are actually toggles (`switchRow`) also carry no affordance beyond the switch,
which is fine — the inconsistency is only that `valueRow` (for example App
version) looks identical to a navigable row but does nothing.

**3.5 No empty states in the library screens.** Bookmarks, history, downloads and
the reading list are lists that can be empty; the extensions screen has a proper
"No extension matches this page" message, but the others should say so too.

---

## 4. Improvements worth doing first

Ordered by how much they change how the app feels, per unit of work.

1. **Put icons on settings rows** (§1.3) — one change in the settings builder plus
   a mapping table; it transforms 33 screens.
2. **Fix the overloaded icons** (§1.4) — nine rows, and the app stops lying about
   what a tap will do.
3. **Give the bottom bar a selected state** (§2.5) — a few lines, and the app
   finally says where you are.
4. **Normalise stroke weight and radii** (§1.1, §2.1) — mechanical, and the whole
   UI tightens up.
5. **Raise the small touch targets to 48dp** (§3.1).
6. **Make the menu card width adaptive** (§2.4) and let long labels wrap.
7. **Add empty states** to bookmarks, history, downloads and the reading list.
8. **Material You** is already wired behind a switch, so a wallpaper palette is one
   tap away on Android 12+.

Glassmorphism or neumorphism would fight the flat, high-contrast dark theme the
app already has. The better direction here is to finish what is there: one icon
family at one weight, one radius scale, one type scale, and honest affordances.

---

## Corrections after implementing

Three of the findings above did not survive contact with the fix, and it is worth
saying so rather than leaving them standing.

- **§3.2 "dead drawables"** — wrong. `handle_bg` is still used by `sheet.xml`, and
  `panel_bg_dark` / `panel_bg_light` are used by the sheet in both themes. They are
  live resources, not leftovers from the menu rework. Nothing was deleted.
- **§3.4 "valueRow shows a chevron"** — wrong. `valueRow` never adds a chevron;
  only `navRow` does. The two conventions were already correct.
- **§3.5 "empty states missing"** — mostly wrong. Bookmarks, history and the
  reading list already print an empty message (`d_empty`, `rl_empty`). Only the
  files screen relies on a note instead of a dedicated empty state.

## What was implemented

- Every icon normalised to a **1.75** stroke weight; `ic_info` is now outline-only.
- **38 new stroke icons**, and a title-to-icon table in the settings builder, so
  all 183 settings rows carry a leading icon without touching a single call site.
- The nine **overloaded menu icons** replaced (`push_pin`, `picture_as_pdf`,
  `movie`, `photo_camera`, `bookmark_add`, `record_voice_over`, `code`, `delete`,
  `select_all`, `help`, `system_update`).
- **Radius tokens** 12 / 16 / 20 / 28 and the **type scale** 11 / 13 / 15 / 18.
- The bottom bar now **tints the active section**, and its labels are 12sp.
- Touch targets raised to 48dp: the tab counter chip, the menu quick actions.
- The menu card width is now `min(320dp, screen − 24dp)`.
- Settings row titles and summaries **ellipsize** instead of clipping.
