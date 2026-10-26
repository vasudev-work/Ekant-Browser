package com.aekantkit

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * One entry point for everything in this kit.
 *
 *     // in Application.onCreate()
 *     AekantKit.init(this)
 *
 *     // on each new WebView
 *     webView.webViewClient = AekantKit.blockingClient(myOwnClient)
 *
 * That is enough for ad and tracker blocking. Everything else is opt-in.
 */
object AekantKit {

    @Volatile private var blocker: AdBlocker? = null

    /** Loads the filter lists once. Safe to call more than once. */
    fun init(context: Context) {
        if (blocker != null) return
        val c = context.applicationContext
        try { AdFilterEngine.ensureLoaded(c) } catch (_: Exception) { }
        blocker = try { AdBlocker(c) } catch (_: Exception) { null }
    }

    // ---------------------------------------------------------------- ad blocking
    /** True when a request should be dropped. */
    fun isAd(url: String): Boolean = blocker?.isBlocked(url) ?: false

    fun blockAds(on: Boolean) { blocker?.blockAds = on }

    fun blockTrackers(on: Boolean) { blocker?.blockTrackers = on }

    fun isAllowed(host: String): Boolean = AdFilterEngine.isAllowed(host)

    fun allow(host: String, on: Boolean) = AdFilterEngine.allow(host, on)

    fun ruleCount(): Int = AdFilterEngine.ruleCount()

    fun blockedCount(): Int = AdFilterEngine.blockedCount

    /**
     * A client that drops blocked requests and delegates everything else to
     * yours, so you keep your own page callbacks.
     */
    fun blockingClient(delegate: WebViewClient? = null): WebViewClient =
        object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                val u = request?.url?.toString()
                if (u != null && isAd(u)) {
                    return WebResourceResponse("text/plain", "utf-8",
                        ByteArrayInputStream(ByteArray(0)))
                }
                return delegate?.shouldInterceptRequest(view, request)
            }
        }

    /** Injects the cosmetic rules for one host (hides ad containers). */
    fun applyCosmetic(webView: WebView, host: String) {
        val css = AdFilterEngine.cosmeticCss(host)
        if (css.isEmpty()) return
        val js = "try{var s=document.getElementById('aekantkit-cosmetic');" +
            "if(!s){s=document.createElement('style');s.id='aekantkit-cosmetic';" +
            "document.documentElement.appendChild(s);}" +
            "s.textContent=" + org.json.JSONObject.quote(css) + ";}catch(e){}"
        try { webView.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    // ---------------------------------------------------------------- privacy
    /** Strips utm_*, fbclid, gclid and friends from a URL. */
    fun cleanUrl(url: String): String = UrlCleaner.clean(url)

    // ---------------------------------------------------------------- media
    /** Start the foreground service that keeps audio playing in the background. */
    fun startBackgroundPlayback(context: Context) = MediaPlaybackService.start(context)

    fun stopBackgroundPlayback(context: Context) = MediaPlaybackService.stop(context)

    fun isPlayingInBackground(): Boolean = MediaPlaybackService.running

    /** Wire the notification's play / next / previous buttons to your player. */
    fun setPlaybackActions(actions: MediaPlaybackService.Actions?) {
        MediaPlaybackService.actions = actions
    }

    /** Holds the audio session open with a silent loop. */
    fun startKeepAlive(context: Context) = KeepAlive.start(context)

    fun stopKeepAlive() = KeepAlive.stop()

    /** Every media file the page has revealed so far. */
    fun mediaFound(): List<Pair<String, String>> = MediaSniffer.all()

    fun mediaReset(host: String) = MediaSniffer.reset(host)

    // ---------------------------------------------------------------- downloads
    /**
     * Downloads in four parallel ranges when the server allows it, otherwise the
     * caller should fall back to DownloadManager. [probe] tells you which.
     */
    fun canSplit(url: String, cookie: String? = null): Long = FastDownloader.probe(url, cookie)

    fun downloadInParts(
        context: Context, url: String, name: String, mime: String?,
        cookie: String? = null, total: Long
    ) = FastDownloader.start(context, url, name, mime, cookie, total)
}
