package com.vasudev.ekant.browser

import android.content.Context
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
    private var ctx: Context? = null

    fun init(context: Context) {
        ctx = context.applicationContext
        if (tts != null) return
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        tts?.language = Locale.getDefault()
                        tts?.setSpeechRate(rate)
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
        speakCurrent()
        ctx?.let { MediaPlaybackService.start(it) }
    }

    private fun speakCurrent() {
        val s = queue.getOrNull(index) ?: return
        try {
            tts?.setSpeechRate(rate)
            tts?.speak(s, TextToSpeech.QUEUE_FLUSH, Bundle(), "aekant-" + index)
        } catch (_: Exception) { }
        listener?.onState(index, queue.size, s, speaking)
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
        if (speaking) speakCurrent() else notify()
    }

    fun prev() {
        if (index <= 0) return
        index--
        if (speaking) speakCurrent() else notify()
    }

    fun setRate(v: Float) {
        rate = v
        try { tts?.setSpeechRate(v) } catch (_: Exception) { }
        if (speaking) speakCurrent() else notify()
    }

    fun rate(): Float = rate

    fun stop() = stopInternal()

    private fun notify() {
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
