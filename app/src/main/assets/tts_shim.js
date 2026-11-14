/*
 * WTRLab TTS shim
 * ----------------
 * Chain-novel sites call the browser's Web Speech API:
 *
 *     var u = new SpeechSynthesisUtterance(chunk);
 *     u.onend = nextChunk;
 *     speechSynthesis.speak(u);
 *
 * Inside an Android WebView window.speechSynthesis exists but is inert - speak()
 * does nothing and no event ever fires, so playback silently stalls.
 *
 * This shim replaces window.speechSynthesis with an object that has the same
 * surface, but forwards every call to the native TextToSpeech engine through
 * window.__ttsBridge (added with addJavascriptInterface). The native side pushes
 * events back through window.__ttsEvent(kind, id, extra), and this file turns
 * them into the ordinary onstart / onend / onboundary / onerror callbacks.
 *
 *     site speak()  ->  shim  ->  __ttsBridge.speak(...)  ->  engine  ->  audio
 *     engine done   ->  __ttsEvent('end', id)  ->  utterance.onend  ->  next chunk
 *
 * The shim is idempotent: injecting it twice is harmless.
 */
(function () {
    'use strict';

    var bridge = window.__ttsBridge || window.NijatvaSpeech || window.AekantSpeech;
    if (!bridge || typeof bridge.speak !== 'function') {
        return; // not running inside the WTRLab shell
    }
    if (window.__nijatvaTtsShim) {
        return;
    }

    var seq = 0;
    var byId = Object.create(null);   // utteranceId -> utterance object
    var order = [];                   // utteranceIds, oldest first
    var paused = false;
    var voices = [];
    var listeners = Object.create(null);

    function warn() {
        try {
            console.warn.apply(console, ['[wtrlab-tts]'].concat([].slice.call(arguments)));
        } catch (e) { /* ignore */ }
    }

    function fire(u, name, ev) {
        if (!u) return;
        var h = u[name];
        if (typeof h === 'function') {
            try { h.call(u, ev); } catch (e) { warn(name, e); }
        }
    }

    function currentUtterance() {
        for (var i = 0; i < order.length; i++) {
            var u = byId[order[i]];
            if (u) return u;
        }
        return null;
    }

    function parseVoices() {
        var list = [];
        try {
            list = JSON.parse(bridge.getVoicesJson() || '[]');
        } catch (e) {
            list = [];
        }
        if (!Array.isArray(list)) list = [];
        voices = list.map(function (v) {
            v = v || {};
            return {
                name: v.name || 'voice',
                voiceURI: v.voiceURI || v.name || 'voice',
                lang: v.lang || '',
                localService: !!v.localService,
                'default': !!v['default']
            };
        });
        return voices;
    }

    function dispatch(type) {
        var ev = { type: type };
        var arr = listeners[type] || [];
        for (var i = 0; i < arr.length; i++) {
            try { arr[i].call(synth, ev); } catch (e) { warn('listener', e); }
        }
        var h = synth['on' + type];
        if (typeof h === 'function') {
            try { h.call(synth, ev); } catch (e) { warn('on' + type, e); }
        }
    }

    // ------------------------------------------------------- utterance objects

    function SpeechSynthesisUtterance(text) {
        if (!(this instanceof SpeechSynthesisUtterance)) {
            return new SpeechSynthesisUtterance(text);
        }
        this.text = text == null ? '' : String(text);
        this.lang = '';
        this.voice = null;
        this.volume = 1;
        this.rate = 1;
        this.pitch = 1;
        this.onstart = null;
        this.onend = null;
        this.onerror = null;
        this.onpause = null;
        this.onresume = null;
        this.onboundary = null;
        this.onmark = null;
        this._id = null;
    }

    // ---------------------------------------------------------------- actions

    function speak(u) {
        if (!u) return;

        var text = u.text == null ? '' : String(u.text);
        var volume = isFinite(u.volume) ? Math.max(0, Math.min(1, u.volume)) : 1;

        // Nothing to say (or muted): behave like the real API and end immediately.
        if (!text.trim() || volume === 0) {
            setTimeout(function () {
                fire(u, 'onend', { type: 'end', charIndex: 0, elapsedTime: 0 });
            }, 0);
            return;
        }

        var id = 'u' + (++seq) + 'x' + (Date.now() % 100000);
        u._id = id;
        byId[id] = u;
        order.push(id);
        paused = false;

        var voiceUri = resolveVoiceUri(u.voice);
        var lang = u.lang || (u.voice && typeof u.voice === 'object' && u.voice.lang) || '';
        var rate = isFinite(u.rate) && u.rate > 0 ? u.rate : 1;
        var pitch = isFinite(u.pitch) && u.pitch > 0 ? u.pitch : 1;

        try {
            bridge.speak(id, text, rate, pitch, lang, volume, voiceUri);
        } catch (e) {
            warn('bridge.speak failed', e);
            delete byId[id];
            var i = order.indexOf(id);
            if (i >= 0) order.splice(i, 1);
            fire(u, 'onerror', { type: 'error', error: 'synthesis-failed', charIndex: 0 });
        }
    }

    function cancel() {
        byId = Object.create(null);
        order.length = 0;
        paused = false;
        try { bridge.cancel(); } catch (e) { warn('bridge.cancel failed', e); }
        // No events fired on purpose: chain readers treat onend as "advance to the
        // next chunk", and a cancel must not advance anything.
    }

    function pause() {
        if (!order.length) return;
        paused = true;
        try { bridge.pause(); } catch (e) { warn('bridge.pause failed', e); }
    }

    function resume() {
        if (!paused) return;
        paused = false;
        try { bridge.resume(); } catch (e) { warn('bridge.resume failed', e); }
    }

    function getVoices() {
        return voices.slice();
    }

    /**
     * utterance.voice may be a SpeechSynthesisVoice object, a friendly label
     * ("English (Australia)") or an engine id. Resolve it to the engine id we
     * hand to the native side.
     */
    function resolveVoiceUri(v) {
        if (!v) return '';
        if (typeof v === 'string') {
            for (var i = 0; i < voices.length; i++) {
                if (voices[i].voiceURI === v || voices[i].name === v) {
                    return voices[i].voiceURI;
                }
            }
            return v;
        }
        return v.voiceURI || v.name || '';
    }

    // ------------------------------------------------------------ fake object

    var synth = {
        onvoiceschanged: null,
        speak: speak,
        cancel: cancel,
        pause: pause,
        resume: resume,
        getVoices: getVoices,
        addEventListener: function (type, fn) {
            if (typeof fn !== 'function') return;
            (listeners[type] = listeners[type] || []).push(fn);
        },
        removeEventListener: function (type, fn) {
            var arr = listeners[type];
            if (!arr) return;
            var i = arr.indexOf(fn);
            if (i >= 0) arr.splice(i, 1);
        },
        dispatchEvent: function (ev) {
            if (ev && ev.type) dispatch(ev.type);
            return true;
        }
    };

    Object.defineProperty(synth, 'speaking', {
        enumerable: true,
        get: function () { return order.length > 0 && !paused; }
    });
    Object.defineProperty(synth, 'pending', {
        enumerable: true,
        get: function () { return order.length > 1; }
    });
    Object.defineProperty(synth, 'paused', {
        enumerable: true,
        get: function () { return paused; }
    });

    try {
        Object.defineProperty(window, 'speechSynthesis', {
            configurable: true,
            enumerable: true,
            get: function () { return synth; }
        });
    } catch (e) {
        try {
            window.speechSynthesis = synth;
        } catch (e2) {
            warn('cannot replace speechSynthesis', e2);
        }
    }

    if (typeof window.SpeechSynthesisUtterance !== 'function') {
        window.SpeechSynthesisUtterance = SpeechSynthesisUtterance;
    }

    // ------------------------------------------------------- native callbacks

    window.__ttsEvent = function (kind, id, extra) {
        if (kind === 'voiceschanged') {
            parseVoices();
            dispatch('voiceschanged');
            return;
        }

        var u = byId[id];

        if (kind === 'start') {
            fire(u, 'onstart', { type: 'start', charIndex: 0, elapsedTime: 0 });
            return;
        }

        if (kind === 'boundary') {
            fire(u, 'onboundary', {
                type: 'boundary',
                charIndex: (extra && extra.charIndex) || 0,
                charLength: (extra && extra.charLength) || 0,
                name: (extra && extra.name) || '',
                elapsedTime: 0
            });
            return;
        }

        if (kind === 'pause') {
            fire(u, 'onpause', { type: 'pause', charIndex: 0 });
            return;
        }

        if (kind === 'resume') {
            fire(u, 'onresume', { type: 'resume', charIndex: 0 });
            return;
        }

        if (kind === 'end') {
            delete byId[id];
            var i = order.indexOf(id);
            if (i >= 0) order.splice(i, 1);
            if (!order.length) paused = false;
            if (u) {
                fire(u, 'onend', {
                    type: 'end',
                    charIndex: u.text ? u.text.length : 0,
                    elapsedTime: 0
                });
            }
            return;
        }

        if (kind === 'error') {
            delete byId[id];
            var j = order.indexOf(id);
            if (j >= 0) order.splice(j, 1);
            if (u) {
                fire(u, 'onerror', {
                    type: 'error',
                    error: typeof extra === 'string' ? extra : 'synthesis-failed',
                    charIndex: 0
                });
            }
            return;
        }
    };

    // ------------------------------------------------------------------ boot

    parseVoices();
    window.__nijatvaTtsShim = {
        version: '1.0.0',
        refreshVoices: parseVoices,
        bridge: bridge
    };

    // Some sites wait for 'voiceschanged' before they will speak at all.
    setTimeout(function () { parseVoices(); dispatch('voiceschanged'); }, 0);
    setTimeout(function () { parseVoices(); dispatch('voiceschanged'); }, 400);
    setTimeout(function () { parseVoices(); dispatch('voiceschanged'); }, 1500);
})();
