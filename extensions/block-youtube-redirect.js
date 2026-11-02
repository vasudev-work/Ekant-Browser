// ==UserScript==
// @name        Block YouTube redirects
// @description Stop ad links from throwing you into YouTube
// @match       *
// @run-at      document-end
// @grant       none
// ==/UserScript==

/*
 * Nijatva extension: a mis-tap on a YouTube ad link does nothing.
 *
 * Plenty of sites carry a "subscribe to our channel" banner, or an ad slot whose
 * whole job is to send you to YouTube. They are often placed right where a
 * thumb goes - under a video, next to a next button - so an ordinary tap ends up
 * on youtube.com with an ad playing.
 *
 * This script blocks that one kind of navigation, on sites that are not YouTube:
 *   - a click on a link to youtube.com / youtu.be / m.youtube.com is cancelled
 *     before the page's own handlers see it, and a small bar offers to open it
 *     anyway (so a deliberate click still works, one extra tap later)
 *   - window.open() to a YouTube URL is refused and reported the same way
 *
 * What it does NOT do, and cannot: a click *inside* a cross-origin YouTube
 * iframe is invisible to page script, so an embedded player can still take you
 * away; and an ad that assigns location.href directly cannot be cancelled from
 * here (Location is unforgeable). Both are the browser's own business - its
 * native filters are the place for that. This handles the link case, which is
 * the one that catches thumbs.
 *
 * Deliberately not on youtube.com itself: there every link is a YouTube link.
 *
 * Pattern: *
 */
(function () {
  try {
    if (window.__nijatvaNoYtRedirect) return; // never run twice
    window.__nijatvaNoYtRedirect = 1;

    var YT = /^https?:\/\/([a-z0-9-]+\.)*(youtube\.com|youtu\.be|youtube-nocookie\.com)(\/|$|\?|#)/i;

    var nativeOpen = null; // the real window.open, kept so "Open" can use it
    var bar = null;
    var hideTimer = null;
    var pendingUrl = '';

    var CSS =
      '#nijatva-nyt-bar{' +
      'position:fixed!important;left:50%!important;bottom:18px!important;' +
      'transform:translateX(-50%)!important;z-index:2147483647!important;' +
      'display:none;align-items:center!important;gap:10px!important;' +
      'max-width:calc(100vw - 24px)!important;padding:9px 12px!important;' +
      'border-radius:10px!important;background:rgba(18,18,20,.94)!important;' +
      'color:#f1f1f3!important;border:1px solid rgba(255,255,255,.12)!important;' +
      'box-shadow:0 8px 30px rgba(0,0,0,.5)!important;' +
      'font:13px/1.4 -apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif!important;' +
      'box-sizing:border-box!important;' +
      '}' +
      '#nijatva-nyt-bar .nyt-msg{flex:1 1 auto!important}' +
      '#nijatva-nyt-bar button{' +
      'flex:0 0 auto!important;padding:5px 10px!important;border-radius:7px!important;' +
      'border:1px solid rgba(255,255,255,.16)!important;background:transparent!important;' +
      'color:#cfcfd6!important;font:12px/1.2 inherit!important;cursor:pointer!important;' +
      '}' +
      '#nijatva-nyt-bar button.nyt-open{' +
      'background:#c4302b!important;border-color:#c4302b!important;color:#fff!important;' +
      '}' +
      '#nijatva-nyt-bar button:hover{opacity:.88!important}';

    function injectCss() {
      try {
        if (document.getElementById('nijatva-nyt-style')) return;
        var s = document.createElement('style');
        s.id = 'nijatva-nyt-style';
        s.textContent = CSS;
        (document.head || document.documentElement).appendChild(s);
      } catch (e) {}
    }

    function el(tag, cls, text) {
      var n = document.createElement(tag);
      if (cls) n.className = cls;
      if (text !== undefined && text !== null) n.textContent = String(text);
      return n;
    }

    function ensureBar() {
      try {
        if (bar && document.documentElement.contains(bar)) return bar;
        bar = el('div');
        bar.id = 'nijatva-nyt-bar';

        bar.appendChild(el('span', 'nyt-msg', 'YouTube link blocked'));

        var open = el('button', 'nyt-open', 'Open');
        open.type = 'button';
        open.addEventListener('click', function (ev) {
          try {
            ev.preventDefault();
            ev.stopPropagation();
            var u = pendingUrl;
            hideBar();
            if (!u) return;
            // use the untouched function: window.open is wrapped below to
            // refuse YouTube, and this click is the user saying yes.
            if (nativeOpen) nativeOpen.call(window, u, '_blank', 'noopener');
            else window.open(u, '_blank', 'noopener');
          } catch (e) {}
        }, false);
        bar.appendChild(open);

        var no = el('button', null, 'Stay');
        no.type = 'button';
        no.addEventListener('click', function (ev) {
          try {
            ev.preventDefault();
            ev.stopPropagation();
            hideBar();
          } catch (e) {}
        }, false);
        bar.appendChild(no);

        document.documentElement.appendChild(bar);
        return bar;
      } catch (e) {
        return null;
      }
    }

    function showBar(url) {
      try {
        if (!ensureBar()) return;
        pendingUrl = url;
        bar.style.setProperty('display', 'flex', 'important');
        if (hideTimer) clearTimeout(hideTimer);
        hideTimer = setTimeout(function () {
          hideTimer = null;
          hideBar();
        }, 6000);
      } catch (e) {}
    }

    function hideBar() {
      try {
        if (hideTimer) {
          clearTimeout(hideTimer);
          hideTimer = null;
        }
        if (bar) bar.style.setProperty('display', 'none', 'important');
        pendingUrl = '';
      } catch (e) {}
    }

    function absolute(href) {
      try {
        if (!href) return '';
        return new URL(href, window.location.href).href;
      } catch (e) {
        return '';
      }
    }

    // Walk up from the click target to the nearest link, so a click on the
    // banner image inside <a> is caught as well.
    function linkFrom(node) {
      try {
        var n = node;
        var depth = 0;
        while (n && n.nodeType === 1 && depth < 8) {
          if (n.tagName === 'A' || n.tagName === 'AREA') return n;
          n = n.parentNode;
          depth++;
        }
        return null;
      } catch (e) {
        return null;
      }
    }

    function onClick(ev) {
      try {
        if (bar && bar.contains(ev.target)) return;
        var a = linkFrom(ev.target);
        if (!a) return;
        var url = absolute(a.getAttribute('href'));
        if (!url || !YT.test(url)) return;

        ev.preventDefault();
        ev.stopImmediatePropagation();
        showBar(url);
      } catch (e) {}
    }

    function onAux(ev) {
      try {
        if (ev.button !== 1) return;
        onClick(ev);
      } catch (e) {}
    }

    function start() {
      try {
        injectCss();

        // Every link on youtube.com is a YouTube link, so this script must not
        // run there at all.
        var host = String(window.location.hostname || '').toLowerCase();
        if (/(^|\.)youtube\.com$/.test(host) ||
            /(^|\.)youtu\.be$/.test(host) ||
            /(^|\.)youtube-nocookie\.com$/.test(host)) {
          return;
        }

        document.addEventListener('click', onClick, true);
        document.addEventListener('auxclick', onAux, true);

        // Ads that call window.open instead of using a link.
        try {
          if (typeof window.open === 'function' && !window.open.__nijatvaWrapped) {
            var orig = window.open;
            nativeOpen = orig;
            var wrapped = function (url) {
              try {
                var abs = absolute(url);
                if (abs && YT.test(abs)) {
                  showBar(abs);
                  return null;
                }
              } catch (e) {}
              return orig.apply(window, arguments);
            };
            wrapped.__nijatvaWrapped = 1;
            window.open = wrapped;
          }
        } catch (e) {}

        document.addEventListener('keydown', function (ev) {
          try {
            if (ev.key === 'Escape' || ev.keyCode === 27) hideBar();
          } catch (e) {}
        }, true);
      } catch (e) {}
    }

    start();
  } catch (e) {}
})();
