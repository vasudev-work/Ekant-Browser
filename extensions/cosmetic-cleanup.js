/*
 * Nijatva extension: remove the blank boxes that ads leave behind.
 *
 * The browser already blocks the requests, which leaves empty rectangles in the
 * page - a gap where the ad would have been. uBlock does this in the page too,
 * with its cosmetic filters. This is our own version: a container is removed
 * only when it matches a known ad pattern AND is empty, so nothing real is
 * hidden by mistake.
 */
(function () {
  try {
    if (window.__nijatvaCosmetic) return;
    window.__nijatvaCosmetic = 1;

    var SEL = [
      '[id^="div-gpt-ad"]', '[id^="google_ads"]', '[data-ad-slot]', '[data-ad-client]',
      '.adsbygoogle', '[class*="ad-slot"]', '[class*="ad-container"]', '[class*="ad-wrapper"]',
      '[class*="advert"]', '[class*="sponsored"]', '[class*="ad-unit"]',
      'iframe[src*="doubleclick.net"]', 'iframe[src*="googlesyndication"]',
      'iframe[src*="adservice"]', 'iframe[src*="amazon-adsystem"]',
      'ins.adsbygoogle', '.ad-placeholder', '.ad-slot-empty'
    ].join(',');

    function blank(el) {
      try {
        if (!el || !el.parentNode) return false;
        if (el.querySelector && el.querySelector('img,video,canvas,input,button,a[href]')) return false;
        var t = (el.textContent || '').replace(/\s+/g, '');
        if (t.length > 0) return false;
        var r = el.getBoundingClientRect ? el.getBoundingClientRect() : null;
        // only things that still take real space
        if (r && (r.width < 40 || r.height < 20)) return false;
        return true;
      } catch (e) { return false; }
    }

    function sweep() {
      try {
        var list = document.querySelectorAll(SEL);
        for (var i = 0; i < list.length; i++) {
          if (blank(list[i])) list[i].remove();
        }
      } catch (e) { }
    }

    var timer = null;
    function soon() {
      if (timer) return;
      timer = setTimeout(function () { timer = null; sweep(); }, 1200);
    }

    sweep();
    document.addEventListener('DOMContentLoaded', soon);
    window.addEventListener('load', soon);
    try {
      var mo = new MutationObserver(soon);
      mo.observe(document.documentElement, { childList: true, subtree: true });
    } catch (e) { }
    setInterval(sweep, 4000);
  } catch (e) { }
})();
