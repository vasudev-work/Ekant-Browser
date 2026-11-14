package com.vasudev.ekant.browser

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import java.lang.ref.WeakReference

/**
 * The bridge a page talks to as `window.speechSynthesis`.
 *
 * The engine itself is [Tts] - a queue, real pause and resume, the device's whole
 * voice list, word boundaries. This class is only the seam between that engine and
 * one WebView: it posts calls onto the main thread, sends events back into the page,
 * and holds the keep-alive while something is being spoken.
 */
class SpeechBridge(webView: WebView) {

    private val view = WeakReference(webView)
    private val ctxRef = WeakReference(webView.context)

    init {
        active = this
        try {
            Tts.onEvent = { js -> active?.post(js) }
            Tts.onStateChanged = { speaking ->
                val c = ctxRef.get()
                if (c != null) {
                    try { if (speaking) KeepAlive.start(c) else KeepAlive.stop() } catch (_: Exception) { }
                }
            }
            val c = webView.context
            // the voice chosen in settings wins when a page does not pick one
            try {
                val p = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                val name = p.getString("tts_voice_name", "") ?: ""
                if (name.isNotBlank()) Tts.preferredVoiceUri = name
            } catch (_: Exception) { }
            Tts.init(c.applicationContext)
        } catch (_: Exception) { }
    }

    private fun post(js: String) {
        try { view.get()?.post { try { view.get()?.evaluateJavascript(js, null) } catch (_: Exception) { } } } catch (_: Exception) { }
    }

    private fun ui(block: () -> Unit) {
        try { view.get()?.post { try { block() } catch (_: Exception) { } } } catch (_: Exception) { }
    }

    @JavascriptInterface
    fun platform(): String = "android-webview/api" + android.os.Build.VERSION.SDK_INT

    @JavascriptInterface
    fun speak(id: String, text: String, rate: Double, pitch: Double, lang: String?, volume: Double, voiceUri: String?) {
        active = this
        ui {
            val c = ctxRef.get() ?: return@ui
            Tts.init(c.applicationContext)
            if (voiceUri.isNullOrBlank()) {
                try {
                    val p = c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                    val name = p.getString("tts_voice_name", "") ?: ""
                    if (name.isNotBlank()) Tts.preferredVoiceUri = name
                } catch (_: Exception) { }
            }
            Tts.speak(id, text, rate.toFloat(), pitch.toFloat(), lang, voiceUri)
        }
    }

    @JavascriptInterface fun cancel() { ui { Tts.cancel() } }
    @JavascriptInterface fun pause() { ui { Tts.pause() } }
    @JavascriptInterface fun resume() { ui { Tts.resume() } }
    @JavascriptInterface fun wake() { ui { ctxRef.get()?.let { Tts.init(it.applicationContext) } } }
    @JavascriptInterface fun engineReady(): Boolean = try { Tts.isReady() } catch (_: Exception) { false }
    @JavascriptInterface fun isSpeaking(): Boolean = try { Tts.isSpeaking() } catch (_: Exception) { false }
    @JavascriptInterface fun isPaused(): Boolean = try { Tts.isPaused() } catch (_: Exception) { false }
    @JavascriptInterface fun getVoicesJson(): String = try { Tts.voicesJson() } catch (_: Exception) { "[]" }

    companion object {
        /** The bridge whose page is speaking, so events go to the right tab. */
        @Volatile private var active: SpeechBridge? = null

        /** The stand-in for speechSynthesis, read from assets once. */
        val SHIM: String
            get() = shimCache ?: try {
                val c = active?.ctxRef?.get()?.applicationContext
                val s = c?.assets?.open("tts_shim.js")?.bufferedReader()?.use { it.readText() } ?: ""
                if (s.isNotBlank()) shimCache = s
                s
            } catch (_: Exception) { "" }

        @Volatile private var shimCache: String? = null
    }
}
