/* Qmamu frontend - talks to the Qmamu meta-search backend and renders its own UI. */
(function () {
  'use strict';

  var API = window.QMAMU_API || '/api/search';

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
    footNote: document.getElementById('footNote')
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

    var url = API + '?q=' + encodeURIComponent(state.q) +
      '&category=' + encodeURIComponent(state.cat) +
      '&page=' + state.page +
      (state.engines.length ? '&engines=' + encodeURIComponent(state.engines.join(',')) : '');

    fetch(url, { headers: { 'Accept': 'application/json' } })
      .then(function (r) { return r.json().then(function (j) { return { ok: r.ok, body: j }; }); })
      .then(function (res) {
        if (mine !== seq) return;             // a newer search took over
        if (!res.ok) throw new Error(res.body.error || 'search failed');
        render(res.body);
      })
      .catch(function (err) {
        if (mine !== seq) return;
        setStatus('Could not reach the search backend.<br><small>' + String(err.message) +
          ' — check that the Qmamu server and its SearXNG container are running.</small>', true);
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

  el.q.value = state.q;
  if (state.q) { sync(); } else { renderChips(); renderTabs(); el.q.focus(); }
})();
