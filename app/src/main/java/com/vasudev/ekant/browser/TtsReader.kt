package com.vasudev.ekant.browser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Reads a page aloud, sentence by sentence, and keeps going while the app is
 * in the background (the media service holds a wake lock).
 */
object TtsReader {

    interface Listener {
        fun onState(index: Int, total: Int, sentence: String, speaking: Boolean)
    }

    var listener: Listener? = null

    private var tts: TextToSpeech? = null
    private var queue: List<String> = emptyList()
    private var index = 0
    private var speaking = false
    private var rate = 1.0f
    private var pitch = 1.0f
    private var sleepMin = 0
    private var sleepHandler: android.os.Handler? = null
    private var ctx: Context? = null
    private var am: android.media.AudioManager? = null
    private var focusPaused = false
    private var noisyReceiver: BroadcastReceiver? = null
    private var batteryReceiver: BroadcastReceiver? = null

    private val focusListener = android.media.AudioManager.OnAudioFocusChangeListener { change ->
        if (!pref("tts_focus")) return@OnAudioFocusChangeListener
        when (change) {
            android.media.AudioManager.AUDIOFOCUS_LOSS,
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (speaking) { focusPaused = true; pause() }
            }
            android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                if (focusPaused) { focusPaused = false; resume() }
            }
        }
    }

    private fun prefFloat(key: String, def: Float): Float = try {
        ctx?.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)?.getFloat(key, def) ?: def
    } catch (_: Exception) { def }

    /** User pronunciation map: "Li Xun" -> "Lee Shurn". */
    private fun applyDict(s: String): String {
        val raw = try {
            ctx?.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)?.getString("tts_dict", "") ?: ""
        } catch (_: Exception) { "" }
        if (raw.isBlank()) return s
        var out = s
        for (line in raw.split("\n")) {
            val i = line.indexOf('\u0001')
            if (i <= 0) continue
            val from = line.substring(0, i).trim()
            val to = line.substring(i + 1)
            if (from.isBlank()) continue
            out = try {
                out.replace(Regex("\\b" + Regex.escape(from) + "\\b", RegexOption.IGNORE_CASE), to)
            } catch (_: Exception) { out }
        }
        return out
    }

    fun setPitch(v: Float) {
        pitch = v
        try { tts?.setPitch(v) } catch (_: Exception) { }
        if (speaking) speakCurrent() else notifyState()
    }

    fun pitch(): Float = pitch

    /** Sleep timer in minutes; 0 turns it off. */
    fun setSleep(minutes: Int) {
        sleepMin = minutes
        sleepHandler?.removeCallbacksAndMessages(null)
        if (minutes <= 0) return
        val h = android.os.Handler(android.os.Looper.getMainLooper())
        sleepHandler = h
        h.postDelayed({ fadeAndPause() }, minutes * 60_000L)
    }

    fun sleepMinutes(): Int = sleepMin

    private fun fadeAndPause() {
        val a = am
        if (a == null) { pause(); return }
        val orig = try { a.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) } catch (_: Exception) { 0 }
        var step = 0
        val h = android.os.Handler(android.os.Looper.getMainLooper())
        val run = object : Runnable {
            override fun run() {
                step++
                try {
                    a.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, orig * (10 - step) / 10, 0)
                } catch (_: Exception) { }
                if (step < 10) {
                    h.postDelayed(this, 300)
                } else {
                    pause()
                    try { a.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, orig, 0) } catch (_: Exception) { }
                }
            }
        }
        h.postDelayed(run, 300)
    }

    private fun pref(key: String): Boolean = try {
        ctx?.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)?.getBoolean(key, true) ?: true
    } catch (_: Exception) { true }

    private fun registerReceivers() {
        val c = ctx ?: return
        try { am = c.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager } catch (_: Exception) { }
        if (noisyReceiver == null) {
            noisyReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY && pref("tts_focus")) pause()
                }
            }
            try {
                c.registerReceiver(noisyReceiver, IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            } catch (_: Exception) { }
        }
        if (batteryReceiver == null) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    try {
                        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
                        if (level >= 0 && scale > 0 && level * 100 / scale < 5 && pref("tts_battery")) pause()
                    } catch (_: Exception) { }
                }
            }
            try {
                c.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            } catch (_: Exception) { }
        }
    }

    private fun requestFocus() {
        if (!pref("tts_focus")) return
        try {
            @Suppress("DEPRECATION")
            am?.requestAudioFocus(focusListener, android.media.AudioManager.STREAM_MUSIC,
                android.media.AudioManager.AUDIOFOCUS_GAIN)
        } catch (_: Exception) { }
    }

    fun init(context: Context) {
        ctx = context.applicationContext
        registerReceivers()
        if (tts != null) return
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        tts?.language = Locale.getDefault()
                        tts?.setSpeechRate(rate)
                        tts?.setPitch(prefFloat("tts_pitch", 1.0f))
                    } catch (_: Exception) { }
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) { }
                        override fun onError(id: String?) { }
                        override fun onDone(id: String?) {
                            if (!speaking) return
                            if (index + 1 < queue.size) { index++; speakCurrent() } else stopInternal()
                        }
                    })
                }
            }
        } catch (_: Exception) { }
    }

    fun isActive(): Boolean = queue.isNotEmpty()
    fun isSpeaking(): Boolean = speaking

    fun start(lines: List<String>) {
        if (lines.isEmpty()) return
        queue = lines
        index = 0
        speaking = true
        requestFocus()
        speakCurrent()
        ctx?.let { MediaPlaybackService.start(it) }
    }

    private fun speakCurrent() {
        val s = queue.getOrNull(index) ?: return
        try {
            tts?.setSpeechRate(rate)
            tts?.setPitch(pitch)
            tts?.speak(applyDict(s), TextToSpeech.QUEUE_FLUSH, Bundle(), "aekant-" + index)
        } catch (_: Exception) { }
        listener?.onState(index, queue.size, s, speaking)
    }

    /** Live page text: append new paragraphs while reading. */
    fun append(text: String) {
        if (queue.isEmpty()) return
        val t = text.trim()
        if (t.length < 40) return
        queue = queue + t
        listener?.onState(index, queue.size, queue.getOrNull(index) ?: "", speaking)
    }

    fun toggle() { if (speaking) pause() else resume() }

    fun pause() {
        speaking = false
        try { tts?.stop() } catch (_: Exception) { }
        listener?.onState(index, queue.size, queue.getOrNull(index) ?: "", false)
    }

    fun resume() {
        if (queue.isEmpty()) return
        speaking = true
        speakCurrent()
    }

    fun next() {
        if (index + 1 >= queue.size) return
        index++
        if (speaking) speakCurrent() else notifyState()
    }

    fun prev() {
        if (index <= 0) return
        index--
        if (speaking) speakCurrent() else notifyState()
    }

    fun setRate(v: Float) {
        rate = v
        try { tts?.setSpeechRate(v) } catch (_: Exception) { }
        if (speaking) speakCurrent() else notifyState()
    }

    fun rate(): Float = rate

    fun stop() = stopInternal()

    private fun notifyState() {
        listener?.onState(index, queue.size, queue.getOrNull(index) ?: "", speaking)
    }

    private fun stopInternal() {
        speaking = false
        queue = emptyList()
        index = 0
        try { tts?.stop() } catch (_: Exception) { }
        try { ctx?.let { MediaPlaybackService.stop(it) } } catch (_: Exception) { }
        listener?.onState(0, 0, "", false)
    }
}
