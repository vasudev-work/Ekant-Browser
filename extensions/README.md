# Aekant extensions

This folder is the extension source the browser reads. In the app, open
**Extensions → From GitHub → Check for extensions** and every entry below appears
with an **+** button; tapping it downloads the script and installs it.

The app fetches:

```
https://raw.githubusercontent.com/vasudev-work/Ekant-Browser/main/extensions/index.json
```

## Add an extension

1. Drop a JavaScript file in this folder, for example `my-script.js`.

   ```js
   (function () {
     try {
       // runs on every page whose URL matches the pattern
       document.body.style.letterSpacing = '0.2px';
     } catch (e) { }
   })();
   ```

2. Add an entry to `index.json`:

   ```json
   {
     "id": "my-script",
     "name": "My script",
     "desc": "One line shown in the app",
     "pattern": "*example.com*",
     "file": "my-script.js",
     "version": "1.0"
   }
   ```

3. Commit. The app picks it up the next time you tap **Check for extensions**.

## Fields

| Field | Meaning |
|---|---|
| `id` | Unique id. Reuse the same id to publish an update. |
| `name` | Shown in the list and in Extensions. |
| `desc` | One line of explanation. |
| `pattern` | Where it runs. `*` matches anything, commas separate alternatives, `*example.com*` matches that site. |
| `file` | The `.js` file in this folder. |
| `version` | Free text, shown next to the description. |

## Notes

- These are **userscripts**, not Chrome/Firefox extensions. The browser injects the
  file into matching pages, so anything you can do with plain JavaScript works;
  Chrome extension APIs (`chrome.*`) do not exist.
- Keep scripts defensive: wrap the body in `try { } catch (e) { }` so a page change
  cannot break browsing.
- Scripts run on every matching page load, so keep them small.
