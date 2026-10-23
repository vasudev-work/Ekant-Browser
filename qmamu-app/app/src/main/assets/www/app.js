/* Qmamu frontend - talks to the Qmamu meta-search backend and renders its own UI. */
(function () {
  'use strict';

  // Backend resolution order: what the user saved here, then what the host page
  // injected (the Android app sets window.QMAMU_API), then same-origin.
  var API = localStorage.getItem('qmamu_api') || window.QMAMU_API || '/api/search';

  // The page speaks two dialects: the Qmamu server's own endpoint, and a plain
  // SearXNG JSON endpoint. Point it at either one.
  function isSearx() {
    return /format=json/.test(API) || (!/\/api\//.test(API) && /searx|\/search/i.test(API));
  }

  function buildUrl(q, engines, cat, page) {
    var sep = API.indexOf('?') >= 0 ? '&' : '?';
    if (isSearx()) {
      var u = API + sep + 'q=' + encodeURIComponent(q) + '&format=json&pageno=' + page;
      if (cat && cat !== 'general') u += '&categories=' + encodeURIComponent(cat);
      return u;
    }
    var parts = [
      'q=' + encodeURIComponent(q),
      'category=' + encodeURIComponent(cat),
      'page=' + page
    ];
    if (engines.length) parts.push('engines=' + encodeURIComponent(engines.join(',')));
    return API + sep + parts.join('&');
  }

  /** Accept a raw SearXNG payload as well as the Qmamu one. */
  function normalise(d) {
    if (d && d.results && typeof d.merged === 'undefined') {
      return {
        query: d.query || state.q,
        results: d.results.map(function (r) {
          return {
            title: r.title || r.url,
            url: r.url,
            snippet: r.content || r.snippet || '',
            thumbnail: r.thumbnail || r.img_src || '',
            engines: r.engines || (r.engine ? [r.engine] : [])
          };
        }),
        raw: d.results.length,
        merged: d.results.length,
        perEngine: {},
        tookMs: 0,
        page: state.page,
        answers: d.answers || []
      };
    }
    return d;
  }

  var ENGINES = [
    { id: 'google',    label: 'Google' },
    { id: 'bing',      label: 'Bing' },
    { id: 'ddg',       label: 'DuckDuckGo' },
    { id: 'brave',     label: 'Brave' },
    { id: 'yandex',    label: 'Yandex' },
    { id: 'startpage', label: 'Startpage' },
    { id: 'mojeek',    label: 'Mojeek' },
    { id: 'wikipedia', label: 'Wikipedia' }
  ];

  var CATS = [
    { id: 'general', label: 'All' },
    { id: 'images',  label: 'Images' },
    { id: 'videos',  label: 'Videos' },
    { id: 'news',    label: 'News' },
    { id: 'map',     label: 'Maps' },
    { id: 'it',      label: 'Code' }
  ];

  var el = {
    form: document.getElementById('form'),
    q: document.getElementById('q'),
    chips: document.getElementById('chips'),
    tabs: document.getElementById('tabs'),
    status: document.getElementById('status'),
    meta: document.getElementById('meta'),
    results: document.getElementById('results'),
    pages: document.getElementById('pages'),
    prev: document.getElementById('prev'),
    next: document.getElementById('next'),
    pageLabel: document.getElementById('pageLabel'),
    footNote: document.getElementById('footNote'),
    filters: document.getElementById('filters'),
    sheet: document.getElementById('sheet'),
    api: document.getElementById('api'),
    apiSave: document.getElementById('apiSave'),
    apiReset: document.getElementById('apiReset'),
    apiClose: document.getElementById('apiClose'),
    apiNow: document.getElementById('apiNow'),
    home: document.getElementById('home')
  };

  // ---- state, seeded from the URL so ?q= and ?engine= work when linked to ----
  var params = new URLSearchParams(location.search);
  var state = {
    q: params.get('q') || '',
    engines: (params.get('engine') || params.get('eng') || '')
      .split(',').map(function (s) { return s.trim(); })
      .filter(function (id) { return ENGINES.some(function (e) { return e.id === id; }); }),
    cat: params.get('cat') || params.get('category') || 'general',
    page: Math.max(1, parseInt(params.get('page') || '1', 10) || 1)
  };

  // ---------------------------------------------------------------- controls
  function renderChips() {
    el.chips.innerHTML = '';
    var all = document.createElement('span');
    all.className = 'chip' + (state.engines.length ? '' : ' on');
    all.textContent = 'All engines';
    all.onclick = function () { state.engines = []; state.page = 1; sync(); };
    el.chips.appendChild(all);

    ENGINES.forEach(function (e) {
      var c = document.createElement('span');
      var on = state.engines.indexOf(e.id) >= 0;
      c.className = 'chip' + (on ? ' on' : '');
      c.textContent = e.label;
      c.onclick = function () {
        var i = state.engines.indexOf(e.id);
        if (i >= 0) state.engines.splice(i, 1); else state.engines.push(e.id);
        state.page = 1;
        sync();
      };
      el.chips.appendChild(c);
    });
  }

  function renderTabs() {
    el.tabs.innerHTML = '';
    CATS.forEach(function (c) {
      var t = document.createElement('span');
      t.className = 'tab' + (state.cat === c.id ? ' on' : '');
      t.textContent = c.label;
      t.onclick = function () { state.cat = c.id; state.page = 1; sync(); };
      el.tabs.appendChild(t);
    });
  }

  // ---------------------------------------------------------------- helpers
  function host(u) {
    try { return new URL(u).host.replace(/^www\./, ''); } catch (e) { return u; }
  }
  function show(node, on) { node.hidden = !on; }

  function setStatus(text, isErr) {
    if (!text) { show(el.status, false); el.status.innerHTML = ''; return; }
    el.status.className = 'status' + (isErr ? ' err' : '');
    el.status.innerHTML = text;
    show(el.status, true);
  }

  // ---------------------------------------------------------------- search
  var seq = 0;

  function search() {
    if (!state.q) {
      setStatus('Type something to search.');
      el.results.innerHTML = '';
      show(el.meta, false);
      show(el.pages, false);
      return;
    }

    var mine = ++seq;
    setStatus('Searching across ' +
      (state.engines.length ? state.engines.length + ' engine(s)' : 'every enabled engine') + '…');
    el.results.innerHTML = '';
    show(el.meta, false);
    show(el.pages, false);

    fetch(buildUrl(state.q, state.engines, state.cat, state.page),
          { headers: { 'Accept': 'application/json' } })
      .then(function (r) { return r.json().then(function (j) { return { ok: r.ok, body: j }; }); })
      .then(function (res) {
        if (mine !== seq) return;             // a newer search took over
        if (!res.ok) throw new Error(res.body.error || 'search failed');
        render(normalise(res.body));
      })
      .catch(function (err) {
        if (mine !== seq) return;
        setStatus('Could not reach the search backend.<br><small>' + String(err.message) +
          '</small><br><button class="inline" id="fixBackend">Set the backend address</button>', true);
        var btn = document.getElementById('fixBackend');
        if (btn) btn.onclick = openSheet;
      });
  }

  // ---------------------------------------------------------------- render
  function render(d) {
    show(el.meta, true);
    var per = Object.keys(d.perEngine || {}).map(function (k) {
      return '<span class="badge">' + k + ' ' + d.perEngine[k] + '</span>';
    }).join('');

    el.meta.innerHTML =
      '<span><b>' + d.merged + '</b> results</span>' +
      '<span>merged from <b>' + (d.raw || 0) + '</b> hits in <b>' + d.tookMs + ' ms</b></span>' +
      (d.cached ? '<span class="badge">cached</span>' : '') +
      per;

    if (d.answers && d.answers.length) {
      var a = document.createElement('div');
      a.className = 'answer';
      a.textContent = d.answers[0];
      el.results.appendChild(a);
    }

    if (!d.results.length) {
      setStatus('No results for “' + d.query + '”. Try another engine or a different wording.');
      show(el.pages, false);
      return;
    }

    setStatus('');

    d.results.forEach(function (r) {
      var li = document.createElement('li');
      var hit = document.createElement('div');
      hit.className = 'hit';

      if (r.thumbnail) {
        var img = document.createElement('img');
        img.className = 'thumb';
        img.src = r.thumbnail;
        img.loading = 'lazy';
        img.alt = '';
        img.onerror = function () { img.remove(); };
        hit.appendChild(img);
      }

      var body = document.createElement('div');
      body.className = 'body';

      var t = document.createElement('a');
      t.className = 't';
      t.href = r.url;
      t.textContent = r.title;
      t.title = r.title;
      body.appendChild(t);

      var u = document.createElement('div');
      u.className = 'u';
      u.textContent = host(r.url);
      body.appendChild(u);

      if (r.snippet) {
        var s = document.createElement('div');
        s.className = 's';
        s.textContent = r.snippet;
        body.appendChild(s);
      }

      if (r.engines && r.engines.length) {
        var box = document.createElement('div');
        box.className = 'engines';
        r.engines.forEach(function (name, i) {
          var tag = document.createElement('span');
          tag.className = 'tag' + (i === 0 ? ' primary' : '');
          tag.textContent = name;
          box.appendChild(tag);
        });
        body.appendChild(box);
      }

      hit.appendChild(body);
      li.appendChild(hit);
      el.results.appendChild(li);
    });

    show(el.pages, true);
    el.pageLabel.textContent = 'Page ' + d.page;
    el.prev.disabled = d.page <= 1;
    el.next.disabled = d.results.length === 0;
  }

  // ---------------------------------------------------------------- wiring
  function sync() {
    renderChips();
    renderTabs();
    var p = new URLSearchParams();
    if (state.q) p.set('q', state.q);
    if (state.cat && state.cat !== 'general') p.set('cat', state.cat);
    if (state.engines.length) p.set('engine', state.engines.join(','));
    if (state.page > 1) p.set('page', String(state.page));
    try { history.replaceState(null, '', p.toString() ? '?' + p.toString() : location.pathname); } catch (e) { }
    search();
  }

  // the logo resets the page without navigating anywhere
  el.home.addEventListener('click', function (ev) {
    ev.preventDefault();
    state.q = '';
    state.page = 1;
    el.q.value = '';
    sync();
    el.q.focus();
  });

  el.form.addEventListener('submit', function (ev) {
    ev.preventDefault();
    state.q = el.q.value.trim();
    state.page = 1;
    sync();
  });

  el.prev.onclick = function () { if (state.page > 1) { state.page--; sync(); } };
  el.next.onclick = function () { state.page++; sync(); };

  document.addEventListener('keydown', function (ev) {
    if (ev.key === '/' && document.activeElement !== el.q) { ev.preventDefault(); el.q.focus(); }
    if (ev.key === 'Escape') el.q.blur();
  });

  // ---------------------------------------------------------------- settings
  function paintApiNow() {
    el.apiNow.textContent = 'In use: ' + API + (isSearx() ? '  (SearXNG endpoint)' : '  (Qmamu server)');
  }
  function openSheet() {
    el.api.value = API;
    el.sheet.hidden = false;
    paintApiNow();
  }
  el.filters.onclick = function () { if (el.sheet.hidden) openSheet(); else el.sheet.hidden = true; };
  el.apiClose.onclick = function () { el.sheet.hidden = true; };
  el.apiSave.onclick = function () {
    var v = el.api.value.trim();
    if (!v) return;
    localStorage.setItem('qmamu_api', v);
    API = v;
    el.sheet.hidden = true;
    paintApiNow();
    if (state.q) sync(); else setStatus('Backend saved: ' + API);
  };
  el.apiReset.onclick = function () {
    localStorage.removeItem('qmamu_api');
    API = window.QMAMU_API || '/api/search';
    el.api.value = API;
    paintApiNow();
  };
  paintApiNow();

  el.q.value = state.q;
  if (state.q) { sync(); } else { renderChips(); renderTabs(); el.q.focus(); }
})();
