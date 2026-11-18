/* Aekant extension: hide the Shorts shelves on YouTube. */
(function () {
  try {
    if (window.__aekantHideShorts) return;
    window.__aekantHideShorts = 1;
    var css = 'ytd-reel-shelf-renderer,ytd-rich-shelf-renderer[is-shorts],' +
              'ytd-guide-entry-renderer a[title="Shorts"],ytm-reel-shelf-renderer{display:none!important}';
    var add = function () {
      if (!document.body) return;
      var s = document.getElementById('aekant-hide-shorts');
      if (!s) {
        s = document.createElement('style');
        s.id = 'aekant-hide-shorts';
        s.textContent = css;
        document.documentElement.appendChild(s);
      }
    };
    add();
    document.addEventListener('DOMContentLoaded', add);
    setInterval(add, 3000);
  } catch (e) { }
})();
