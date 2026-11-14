package com.vasudev.ekant.browser

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayDeque
import java.util.Locale

/**
 * Process-wide TextToSpeech holder.
 *
 * The engine lives here (not inside an Activity and not inside the Service) so that
 * a single engine instance survives configuration changes and keeps speaking even
 * when the Activity that started it is gone. [MediaPlaybackService] only provides
 * the foreground notification, the wake lock and the keep-alive audio anchor.
 *
 * Events are pushed back into the WebView as small JS calls:
 *     window.__ttsEvent('start' | 'end' | 'error' | 'boundary' | 'pause' | 'resume',
 *                       utteranceId, extra)
 * which the injected shim (assets/tts_shim.js) translates into the standard
 * Web Speech API callbacks (utterance.onend etc.).
 */
object Tts : TextToSpeech.OnInitListener {

    data class Req(
        val id: String,
        val text: String,
        val rate: Float,
        val pitch: Float,
        val lang: String?,
        val voiceUri: String?
    )

    private var engine: TextToSpeech? = null
    private var ready = false
    private var failed = false

    private val pending = ArrayDeque<Req>()
    private val active = HashMap<String, Req>()
    private val activeOrder = ArrayList<String>()
    private var current: Req? = null
    private var paused = false

    @Volatile
    private var voicesCache: String = "[]"

    private val sleepHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var sleepTask: Runnable? = null

    /** Stops playback after [minutes]; 0 cancels a running timer. */
    fun setSleepTimer(minutes: Int) {
        sleepTask?.let { sleepHandler.removeCallbacks(it) }
        sleepTask = null
        if (minutes <= 0) return
        val task = Runnable { cancel() }
        sleepTask = task
        sleepHandler.postDelayed(task, minutes * 60_000L)
    }

    /** Voice picked in the reader's settings; used when the page does not pick one. */
    @Volatile
    var preferredVoiceUri: String? = null

    /** Set by MainActivity: receives ready-to-eval JavaScript. */
    var onEvent: ((String) -> Unit)? = null

    /** Set by the foreground service: refresh the notification (optional). */
    var onStateChanged: (() -> Unit)? = null

    // ---------------------------------------------------------------- lifecycle

    fun init(ctx: Context) {
        if (engine != null) return
        engine = TextToSpeech(ctx.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            failed = true
            ready = false
            emitEvent("error", "init", jsStr("tts_init_failed"))
            return
        }
        ready = true
        failed = false
        engine?.setOnUtteranceProgressListener(listener)
        refreshVoices()
        while (pending.isNotEmpty()) {
            speakNow(pending.removeFirst())
        }
        emit("window.__ttsEvent&&window.__ttsEvent('voiceschanged','',null);")
    }

    fun shutdown() {
        cancel()
        runCatching { engine?.shutdown() }
        engine = null
        ready = false
    }

    // -------------------------------------------------------------------- speak

    fun speak(id: String, text: String, rate: Float, pitch: Float, lang: String?, voiceUri: String?) {
        if (failed) {
            emitEvent("error", id, jsStr("tts_init_failed"))
            return
        }
        if (text.isBlank()) {
            emitEvent("end", id, null)
            return
        }
        val req = Req(id, text, rate, pitch, lang, voiceUri)
        if (!ready) {
            pending.addLast(req)
            return
        }
        speakNow(req)
    }

    private fun speakNow(req: Req) {
        val e = engine ?: return
        // An explicitly chosen voice wins; the language tag is only a fallback.
        var voiceApplied = false
        val wanted = req.voiceUri?.takeIf { it.isNotBlank() } ?: preferredVoiceUri
        if (!wanted.isNullOrBlank()) {
            val target = runCatching { e.voices?.firstOrNull { it.name == wanted } }.getOrNull()
            if (target != null) {
                voiceApplied = runCatching { e.voice = target }.isSuccess
            }
        }
        if (!voiceApplied) {
            val tag = req.lang
            if (!tag.isNullOrBlank()) {
                runCatching { e.setLanguage(Locale.forLanguageTag(tag)) }
            }
        }
        runCatching { e.setSpeechRate(req.rate.coerceIn(0.1f, 4.0f)) }
        runCatching { e.setPitch(req.pitch.coerceIn(0.1f, 3.0f)) }

        active[req.id] = req
        if (!activeOrder.contains(req.id)) activeOrder.add(req.id)
        current = req

        val params = Bundle()
        params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        val mode = if (paused) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        runCatching { e.speak(req.text, mode, params, req.id) }
        onStateChanged?.invoke()
    }

    // ------------------------------------------------------------- transport ops

    fun pause() {
        if (!ready) return
        paused = true
        runCatching { engine?.stop() }
        emitEvent("pause", current?.id, null)
        onStateChanged?.invoke()
    }

    fun resume() {
        if (!ready) return
        paused = false

        // Android TTS has no real pause: stop() emptied the engine queue, so every
        // utterance we had accepted has to be handed back to the engine, in order,
        // with its ORIGINAL utterance id. That way the page sees exactly one 'end'
        // per chunk and the chain never advances twice or stalls.
        val remaining = activeOrder.mapNotNull { active[it] }
        if (remaining.isEmpty()) {
            onStateChanged?.invoke()
            return
        }
        current?.let { emitEvent("resume", it.id, null) }
        for (req in remaining) speakNow(req)
    }

    fun cancel() {
        pending.clear()
        active.clear()
        activeOrder.clear()
        current = null
        paused = false
        runCatching { engine?.stop() }
        onStateChanged?.invoke()
    }

    // -------------------------------------------------------------------- state

    fun isReady(): Boolean = ready

    fun isPaused(): Boolean = paused

    fun isSpeaking(): Boolean = ready && !paused && active.isNotEmpty()

    fun voicesJson(): String {
        if (voicesCache == "[]") refreshVoices()
        return voicesCache
    }

    fun refreshVoices() {
        val e = engine ?: return
        val voices: Set<Voice>? = runCatching { e.voices }.getOrNull()
        val def = runCatching { e.defaultVoice }.getOrNull()

        // Every engine voice is listed - nothing is merged away, because one
        // language usually has several speakers (Hindi male / Hindi female ...).
        // Engine ids are opaque ("hi-in-x-hia-local"), so each entry is labelled
        // with the locale's display name plus whatever tells it apart.
        val rows = ArrayList<VoiceRow>()
        if (voices != null) {
            for (v in voices) {
                val locale = runCatching { v.locale }.getOrNull() ?: continue
                val label = runCatching { prettify(locale.getDisplayName(Locale.ENGLISH)) }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?: v.name
                val tag = runCatching { locale.toLanguageTag() }.getOrDefault("")
                val local = !v.isNetworkConnectionRequired
                val isDefault = def != null && v.name == def.name
                rows.add(VoiceRow(label, voiceDetail(v), v.name, tag, local, isDefault))
            }
        }

        // Only disambiguate when a language really has more than one voice.
        val perLabel = rows.groupingBy { it.label }.eachCount()

        // English variants first (that is the reading language for most people),
        // then everything else alphabetically; inside a language, offline first.
        val sorted = rows.sortedWith(
            compareBy(
                { if (it.tag.startsWith("en")) 0 else 1 },
                { it.label },
                { if (it.local) 0 else 1 },
                { it.detail }
            )
        )

        val seen = HashMap<String, Int>()
        val arr = JSONArray()
        for (row in sorted) {
            var name = row.label
            if ((perLabel[row.label] ?: 0) > 1 && row.detail.isNotBlank()) {
                name = row.label + " - " + row.detail
            }
            // Last resort: two voices can still produce the same string.
            val n = (seen[name] ?: 0) + 1
            seen[name] = n
            if (n > 1) name = "$name ($n)"

            val o = JSONObject()
            o.put("name", name)          // what the UI shows
            o.put("voiceURI", row.id)    // what we send back to select it
            o.put("lang", row.tag)
            o.put("localService", row.local)
            o.put("default", row.def)
            arr.put(o)
        }
        voicesCache = arr.toString()
    }

    private data class VoiceRow(
        val label: String,
        val detail: String,
        val id: String,
        val tag: String,
        val local: Boolean,
        val def: Boolean
    )

    /**
     * Something that tells two voices of the same language apart.
     * Engines sometimes publish the gender as a feature; when they do not, the
     * engine id is the only honest hint we have ("hi-in-x-hia-local" -> hia).
     */
    private fun voiceDetail(v: Voice): String {
        val feats = runCatching { v.features }.getOrNull()
            ?.map { it.lowercase(Locale.ENGLISH) }
            ?: emptyList()
        // "female" contains "male" - check the longer word first.
        if (feats.any { it.contains("female") }) return "Female"
        if (feats.any { it.contains("male") }) return "Male"

        val parts = v.name.split("-")
        val variant = when {
            parts.size >= 5 && parts[parts.size - 2].length in 1..4 -> parts[parts.size - 2]
            parts.size >= 2 -> parts.last()
            else -> ""
        }
        val mode = if (v.isNetworkConnectionRequired) "online" else "offline"
        return if (variant.isBlank()) mode else "$variant ($mode)"
    }

    /**
     * CLDR renamed a few of these between versions ("Bengali" -> "Bangla",
     * "Hong Kong" -> "Hong Kong SAR China"). The reader's list keeps the older,
     * shorter wording, so normalise whichever version the device's ICU reports.
     */
    private fun prettify(label: String): String = label
        .replace("Bangla", "Bengali")
        .replace("(Hong Kong SAR China)", "(Hong Kong)")
        .replace("(Hong Kong SAR)", "(Hong Kong)")

    // ------------------------------------------------------------------ listener

    private val listener = object : UtteranceProgressListener() {

        override fun onStart(utteranceId: String?) {
            emitEvent("start", utteranceId, null)
            onStateChanged?.invoke()
        }

        override fun onDone(utteranceId: String?) {
            active.remove(utteranceId)
            activeOrder.remove(utteranceId)
            if (current?.id == utteranceId) current = null
            if (active.isEmpty()) paused = false
            emitEvent("end", utteranceId, null)
            onStateChanged?.invoke()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            // Pause / cancel are handled explicitly by pause() / cancel().
        }

        // Abstract and deprecated on the platform, but still called on older APIs.
        @Suppress("OVERRIDE_DEPRECATION")
        override fun onError(utteranceId: String?) {
            onError(utteranceId, TextToSpeech.ERROR)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            active.remove(utteranceId)
            activeOrder.remove(utteranceId)
            if (current?.id == utteranceId) current = null
            emitEvent("error", utteranceId, jsStr("synthesis-failed"))
            onStateChanged?.invoke()
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            val extra = "{\"charIndex\":$start,\"charLength\":${(end - start).coerceAtLeast(0)}}"
            emitEvent("boundary", utteranceId, extra)
        }
    }

    // -------------------------------------------------------------------- utils

    private fun emit(js: String) {
        runCatching { onEvent?.invoke(js) }
    }

    fun emitEvent(kind: String, id: String?, extraJson: String?) {
        val sb = StringBuilder("window.__ttsEvent&&window.__ttsEvent(")
        sb.append(jsStr(kind)).append(',')
        sb.append(jsStr(id ?: ""))
        sb.append(',')
        sb.append(extraJson ?: "null")
        sb.append(");")
        emit(sb.toString())
    }

    fun jsStr(s: String?): String {
        val b = StringBuilder("\"")
        for (c in (s ?: "")) {
            when {
                c == '\\' -> b.append("\\\\")
                c == '"' -> b.append("\\\"")
                c == '\n' -> b.append("\\n")
                c == '\r' -> b.append("\\r")
                c == '\t' -> b.append("\\t")
                c < ' ' -> b.append("\\u").append(String.format("%04x", c.code))
                else -> b.append(c)
            }
        }
        return b.append('"').toString()
    }
}
