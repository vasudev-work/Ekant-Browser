package com.aekant.kit

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * Drop-in browser kit: EasyList/uBlock ad-blocking, cosmetic filtering and
 * background audio, in three calls.
 *
 *   AekantKit.install(this)                                    // once
 *   webView.webViewClient = AekantKit.client(this, myClient)   // per WebView
 *   AekantKit.enableBackgroundAudio(webView, this)             // optional
 */
object AekantKit {

    /** Load the bundled filter lists (on a background thread). Call once. */
    @JvmStatic
    fun install(context: Context) {
        AdFilterEngine.enabled = true
        AdFilterEngine.ensureLoaded(context.applicationContext)
    }

    /** Turn network + cosmetic filtering on or off at runtime. */
    @JvmStatic
    fun setAdBlocking(on: Boolean) { AdFilterEngine.enabled = on }

    /** How many network rules are loaded (handy for a settings screen). */
    @JvmStatic
    fun ruleCount(): Int = AdFilterEngine.ruleCount()

    /** Remove tracking parameters (utm_*, fbclid, gclid, ...) from a link. */
    @JvmStatic
    fun cleanUrl(url: String): String = UrlCleaner.clean(url)

    /**
     * Wrap your own WebViewClient (or pass null) to get ad-blocking, cosmetic
     * filtering and the media listener for background audio.
     */
    @JvmStatic
    @JvmOverloads
    fun client(context: Context, base: WebViewClient? = null): WebViewClient =
        KitClient(context.applicationContext, base)

    /** Keep audio playing when the app is backgrounded or the screen is off. */
    @JvmStatic
    fun enableBackgroundAudio(webView: WebView, context: Context) {
        webView.settings.mediaPlaybackRequiresUserGesture = false
        try { webView.addJavascriptInterface(KitBridge(context.applicationContext), "AekantMedia") } catch (_: Exception) { }
    }

    /** Stop the service and the keep-alive loop. */
    @JvmStatic
    fun stopBackgroundAudio(context: Context) {
        MediaPlaybackService.stop(context)
        KeepAlive.stop()
    }
}

private class KitBridge(private val context: Context) {
    @android.webkit.JavascriptInterface
    fun playing(on: Boolean) {
        if (on) {
            MediaPlaybackService.start(context)
            KeepAlive.start(context)
        } else {
            MediaPlaybackService.stop(context)
            KeepAlive.stop()
        }
    }
}

private class KitClient(
    private val context: Context,
    private val base: WebViewClient?
) : WebViewClient() {

    override fun shouldInterceptRequest(v: WebView?, r: WebResourceRequest?): WebResourceResponse? {
        val url = r?.url?.toString()
        if (url != null && AdFilterEngine.isBlocked(url)) {
            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
        }
        return base?.shouldInterceptRequest(v, r)
    }

    override fun shouldOverrideUrlLoading(v: WebView?, r: WebResourceRequest?): Boolean =
        base?.shouldOverrideUrlLoading(v, r) ?: false

    override fun onPageFinished(v: WebView?, url: String?) {
        base?.onPageFinished(v, url)
        val w = v ?: return
        val host = try { Uri.parse(url ?: return).host ?: return } catch (_: Exception) { return }
        val css = AdFilterEngine.cosmeticCss(host)
        if (css.isNotEmpty()) {
            try {
                w.evaluateJavascript(
                    "try{var s=document.getElementById('aekantkit');" +
                        "if(!s){s=document.createElement('style');s.id='aekantkit';" +
                        "document.documentElement.appendChild(s);}s.textContent=" +
                        org.json.JSONObject.quote(css) + ";}catch(e){}", null)
            } catch (_: Exception) { }
        }
        try { w.evaluateJavascript(MEDIA_LISTENER_JS, null) } catch (_: Exception) { }
    }

    companion object {
        const val MEDIA_LISTENER_JS =
            "(function(){if(window.__aekantkitMedia){return;}window.__aekantkitMedia=1;" +
                "function n(p){try{window.AekantMedia&&window.AekantMedia.playing(p);}catch(e){}}" +
                "['play','playing'].forEach(function(e){document.addEventListener(e,function(){n(true);},true);});" +
                "['pause','ended','emptied'].forEach(function(e){document.addEventListener(e,function(){n(false);},true);});})();"
    }
}
