package com.vasudev.ekant.browser

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * Android WebView does not implement the Web Speech API, so any page that calls
 * window.speechSynthesis.speak() fails with "Speech synthesis not supported".
 * This bridge stands in for it: the shim in [SHIM] installs a speechSynthesis
 * object in the page before the page's own scripts run, and every call lands
 * here on the device's own TextToSpeech engine.
 *
 * Three things make it behave like the real API rather than a toy:
 *  - the engine's real voice list is handed to getVoices(), so a site that picks
 *    a voice by name finds it;
 *  - long text is split into sentence-sized pieces and queued, so a whole
 *    article survives instead of dying partway;
 *  - the genuine end-of-speech callback is passed back, so scripts that chain
 *    utterance after utterance keep going.
 */
class SpeechBridge(webView: WebView) {

    private val view = WeakReference(webView)

    init {
        try { TtsReader.onBoundary = { s, e -> boundary(s, e) } } catch (_: Exception) { }
    }
    private var tts: TextToSpeech? = null
    private var started = false
    private var failed = false
    private var pending: String? = null
    private var lastUtterance = ""

    private fun ctx(): Context? = view.get()?.context

    private fun prefs() = ctx()?.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    private fun attach() {
        if (tts != null) return
        val c = ctx() ?: return
        try {
            tts = TextToSpeech(c) { status ->
                started = status == TextToSpeech.SUCCESS
                failed = !started
                if (!started) return@TextToSpeech
                try {
                    tts?.language = Locale.getDefault()
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) { }
                        override fun onError(id: String?) { finish(false) }
                        override fun onDone(id: String?) {
                            // only the last piece of a split utterance ends the speech
                            if (id == null || id == lastUtterance) finish(true)
                        }
                    })
                } catch (_: Exception) { }
                pending?.let { say(it, "", 1f, 1f) }
                pending = null
            }
        } catch (_: Exception) { failed = true }
    }

    /** Called by TtsReader as it speaks: hand the offsets to the page. */
    fun boundary(start: Int, end: Int) {
        try {
            view.get()?.post {
                try {
                    view.get()?.evaluateJavascript(
                        "window.__nijatvaSpeechBoundary&&window.__nijatvaSpeechBoundary($start,$end)",
                        null)
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    private fun finish(ok: Boolean) {
        try { KeepAlive.stop() } catch (_: Exception) { }
        try {
            view.get()?.post {
                try {
                    view.get()?.evaluateJavascript(
                        "window.__nijatvaSpeechDone&&window.__nijatvaSpeechDone(" + ok + ")", null)
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    /** Sentence-sized pieces, so a long article does not break halfway. */
    private fun pieces(text: String, max: Int = 280): List<String> {
        if (text.length <= max) return listOf(text)
        val out = ArrayList<String>()
        var rest = text
        while (rest.length > max) {
            var cut = -1
            for (i in max downTo max / 2) {
                val c = rest[i]
                if (c == '.' || c == '!' || c == '?' || c == ';' || c == '\n') { cut = i + 1; break }
            }
            if (cut <= 0) for (i in max downTo max / 2) if (rest[i] == ' ') { cut = i + 1; break }
            if (cut <= 0) cut = max
            out.add(rest.substring(0, cut))
            rest = rest.substring(cut)
        }
        if (rest.isNotEmpty()) out.add(rest)
        return out
    }

    private fun say(text: String, lang: String, rate: Float, pitch: Float) {
        if (text.isBlank() || tts == null) return
        try {
            // the page wins; otherwise the voice chosen in settings, otherwise the system
            val chosen = try { prefs()?.getString("tts_voice_lang", "") ?: "" } catch (_: Exception) { "" }
            val want = if (lang.isNotBlank()) lang else chosen
            if (want.isNotBlank()) {
                val l = Locale.forLanguageTag(want)
                if (tts?.isLanguageAvailable(l) == TextToSpeech.LANG_AVAILABLE) tts?.language = l
            }
            val voiceName = try { prefs()?.getString("tts_voice_name", "") ?: "" } catch (_: Exception) { "" }
            if (voiceName.isNotBlank()) {
                try {
                    tts?.voices?.firstOrNull { it.name == voiceName }?.let { tts?.voice = it }
                } catch (_: Exception) { }
            }
            // a site that asks for normal speed gets the reader's own preference
            val prefRate = try { prefs()?.getFloat("tts_rate", 1.0f) ?: 1.0f } catch (_: Exception) { 1.0f }
            val useRate = if (rate in 0.2f..4f) (if (rate == 1f) prefRate else rate) else prefRate
            tts?.setSpeechRate(useRate)
            tts?.setPitch(if (pitch in 0.2f..4f) pitch else 1f)
            try { KeepAlive.start(ctx() ?: return) } catch (_: Exception) { }

            // a page-wide switch: some engines behave better with one long utterance
            val chunk = try { prefs()?.getBoolean("tts_chunk", true) ?: true } catch (_: Exception) { true }
            val parts = if (chunk) pieces(text) else listOf(text)
            lastUtterance = "nijatva-web-" + (parts.size - 1)
            parts.forEachIndexed { i, p ->
                tts?.speak(p, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    null, "nijatva-web-" + i)
            }
        } catch (_: Exception) { }
    }

    @JavascriptInterface
    fun speak(text: String?, lang: String?, rate: Double, pitch: Double) {
        val t = text ?: return
        attach()
        if (failed) { finish(false); return }
        if (!started) { pending = t; return }
        say(t, lang ?: "", rate.toFloat(), pitch.toFloat())
    }

    @JavascriptInterface
    fun stop() {
        try { tts?.stop() } catch (_: Exception) { }
        try { KeepAlive.stop() } catch (_: Exception) { }
    }

    @JavascriptInterface fun pause() { stop() }
    @JavascriptInterface fun resume() { }

    /** The engine's real voices, as the shape getVoices() is meant to return. */
    @JavascriptInterface
    fun voicesJson(): String {
        attach()
        val sb = StringBuilder("[")
        var first = true
        try {
            val voices = tts?.voices ?: emptySet()
            for (v in voices) {
                val name = v.name ?: continue
                val tag = try { v.locale?.toLanguageTag() ?: "" } catch (_: Exception) { "" }
                if (!first) sb.append(',')
                sb.append("{\"voiceURI\":").append(q(name))
                    .append(",\"name\":").append(q(name))
                    .append(",\"lang\":").append(q(tag))
                    .append(",\"localService\":").append(!v.isNetworkConnectionRequired)
                    .append(",\"default\":").append(first)
                    .append(",\"voice\":true}")
                first = false
            }
        } catch (_: Exception) { }
        return sb.append("]").toString()
    }

    /** For the diagnostics screen: is the engine up, and how many voices. */
    @JavascriptInterface
    fun infoJson(): String {
        attach()
        val count = try { tts?.voices?.size ?: 0 } catch (_: Exception) { 0 }
        return "{\"ready\":" + started + ",\"voices\":" + count + ",\"failed\":" + failed + "}"
    }

    private fun q(s: String): String {
        val sb = StringBuilder("\"")
        for (c in s) when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> if (c < ' ') sb.append(' ') else sb.append(c)
        }
        return sb.append('"').toString()
    }

    companion object {
        /**
         * Installs a stand-in speechSynthesis. Injected at page start, before the
         * page's own scripts run, so feature detection sees it.
         */
        val SHIM: String = """
(function(){
  try{
    if (window.speechSynthesis && window.speechSynthesis.speak && window.speechSynthesis.__nijatva) return;
    var B = window.NijatvaSpeech;
    if (!B) return;
    function U(t){
      this.text = t || ''; this.lang = ''; this.rate = 1; this.pitch = 1; this.volume = 1;
      this.voice = null;
      this.onstart = null; this.onend = null; this.onerror = null;
      this.onpause = null; this.onresume = null; this.onboundary = null;
    }
    if (!window.SpeechSynthesisUtterance) window.SpeechSynthesisUtterance = U;
    var fallback = [{voiceURI:'nijatva', name:'Nijatva', lang:(navigator.language||'en'),
                     localService:true, 'default':true, voice:true}];
    var list = [];
    var cur = null;
    var S = {
      __nijatva: true,
      speaking: false, pending: false, paused: false, onvoiceschanged: null,
      speak: function(u){
        try{
          var txt = (u && u.text) || '';
          if (!txt) return;
          cur = u;
          var lang = (u && u.lang) || (u && u.voice && u.voice.lang) || '';
          B.speak(txt, lang, (u && u.rate) || 1, (u && u.pitch) || 1);
          S.speaking = true; S.paused = false; S.pending = false;
          if (u && u.onstart) setTimeout(function(){ try{ u.onstart({}) }catch(e){} }, 0);
        }catch(e){}
      },
      cancel: function(){ try{ B.stop() }catch(e){} S.speaking = false; cur = null; },
      pause: function(){ try{ B.pause() }catch(e){} S.paused = true; },
      resume: function(){ try{ B.resume() }catch(e){} S.paused = false; },
      getVoices: function(){ return list.length ? list : fallback; },
      addEventListener: function(){}, removeEventListener: function(){}
    };
    /* a reader highlights the word it is speaking from these */
    window.__nijatvaSpeechBoundary = function(start, end){
      try{
        if (cur && cur.onboundary) cur.onboundary({
          charIndex: start, charLength: Math.max(0, end - start), name: 'word'
        });
      }catch(e){}
    };
    window.__nijatvaSpeechPaused = function(p){
      try{
        S.paused = !!p;
        if (cur && p && cur.onpause) cur.onpause({});
        if (cur && !p && cur.onresume) cur.onresume({});
      }catch(e){}
    };
    window.__nijatvaSpeechDone = function(ok){
      S.speaking = false;
      try{
        if (cur && ok && cur.onend) cur.onend({});
        if (cur && !ok && cur.onerror) cur.onerror({error:'synthesis-failed'});
      }catch(e){}
    };
    try { Object.defineProperty(window, 'speechSynthesis', {value:S, configurable:true}); }
    catch(e){ window.speechSynthesis = S; }
    /* the engine's own voices, once they are available */
    try{
      var raw = B.voicesJson();
      if (raw && raw.length > 2) list = JSON.parse(raw) || [];
    }catch(e){}
    setTimeout(function(){
      if (!list.length) { try{ var r2 = B.voicesJson(); if (r2 && r2.length > 2) list = JSON.parse(r2) || []; }catch(e){} }
      if (S.onvoiceschanged) { try{ S.onvoiceschanged({type:'voiceschanged'}) }catch(e){} }
      try{ document.dispatchEvent(new Event('voiceschanged')); }catch(e){}
    }, 250);
  }catch(e){}
})();
""".trimIndent()
    }
}
