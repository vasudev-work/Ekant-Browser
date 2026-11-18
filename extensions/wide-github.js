/* Aekant extension: let GitHub use the full width of the window. */
(function () {
  try {
    var css = '.container-xl{max-width:100%!important}' +
              '.application-main{max-width:100%!important}';
    var s = document.getElementById('aekant-wide-github');
    if (!s) {
      s = document.createElement('style');
      s.id = 'aekant-wide-github';
      s.textContent = css;
      document.documentElement.appendChild(s);
    }
  } catch (e) { }
})();
