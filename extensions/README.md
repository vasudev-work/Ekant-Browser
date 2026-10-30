# Nijatva extensions

Userscripts the browser can fetch from **Settings → Extensions → Extension source**.

The default source is this folder:

    https://raw.githubusercontent.com/vasudev-work/nijatva-browser/main/extensions/

`index.json` lists what is available; each entry names a `.js` file in the same
folder. Tapping "Check for extensions" reads that index and offers whatever is
not installed yet.

## What these are, and what they are not

These are **userscripts** - JavaScript injected into pages whose URL matches the
entry's pattern. They are not Chrome extensions: Manifest V2/V3 needs a Chromium
engine with an extension API, and Android WebView has none.

That is also why uBlock cannot be shipped here. uBlock (uBlock-LLC/uBlock) is an
archived GPL-3.0 Chrome/Firefox extension that drives `chrome.webRequest`; there
is no way to run it inside a WebView. Its *job* - blocking ad and tracker
requests - is done natively in this browser, before the page loads, with the
bundled filter lists. What a userscript can add on top is the cleanup:
`cosmetic-cleanup.js` removes the blank boxes that blocking leaves behind.

## Adding one

1. Drop a `.js` file in this folder.
2. Add an entry to `index.json`:

```json
{
  "id": "my-script",
  "name": "My script",
  "desc": "What it does, in one line",
  "pattern": "*example.com*",
  "file": "my-script.js",
  "version": "1.0"
}
```

`pattern` takes `*` wildcards and several comma-separated alternatives.

## Licence

These scripts are original work for this repository. Nothing here is copied from
uBlock or any other GPL project.
