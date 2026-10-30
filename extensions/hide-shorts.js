/* Nijatva extension: hide the Shorts shelves on YouTube. */
(function () {
  try {
    if (window.__nijatvaHideShorts) return;
    window.__nijatvaHideShorts = 1;
    var css = 'ytd-reel-shelf-renderer,ytd-rich-shelf-renderer[is-shorts],' +
              'ytd-guide-entry-renderer a[title="Shorts"],ytm-reel-shelf-renderer{display:none!important}';
    var add = function () {
      if (!document.body) return;
      var s = document.getElementById('nijatva-hide-shorts');
      if (!s) {
        s = document.createElement('style');
        s.id = 'nijatva-hide-shorts';
        s.textContent = css;
        document.documentElement.appendChild(s);
      }
    };
    add();
    document.addEventListener('DOMContentLoaded', add);
    setInterval(add, 3000);
  } catch (e) { }
})();
