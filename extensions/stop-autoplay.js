/*
 * Nijatva extension: stop videos that start on their own.
 *
 * A video is paused if it begins playing without the user having clicked or
 * tapped the page. Once they do, playback is left alone.
 */
(function () {
  try {
    if (window.__nijatvaNoAutoplay) return;
    window.__nijatvaNoAutoplay = 1;
    var touched = false;
    function mark() { touched = true; }
    document.addEventListener('click', mark, true);
    document.addEventListener('touchstart', mark, true);
    document.addEventListener('keydown', mark, true);

    function guard(v) {
      try {
        v.addEventListener('playing', function () {
          if (!touched && !v.muted) { v.pause(); }
        }, true);
      } catch (e) { }
    }

    function scan() {
      try {
        var vs = document.querySelectorAll('video');
        for (var i = 0; i < vs.length; i++) if (!vs[i].__nijatvaGuard) { vs[i].__nijatvaGuard = 1; guard(vs[i]); }
      } catch (e) { }
    }

    scan();
    document.addEventListener('DOMContentLoaded', scan);
    try {
      var mo = new MutationObserver(scan);
      mo.observe(document.documentElement, { childList: true, subtree: true });
    } catch (e) { }
  } catch (e) { }
})();
