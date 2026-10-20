package com.vasudev.ekant.gecko

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

/**
 * GeckoView spike: the same browser ideas as the WebView build, but rendered by
 * Mozilla's non-Chromium Gecko engine. Each "container" is a GeckoSession with its
 * own contextId, which gives real per-container storage isolation.
 */
class MainActivity : Activity() {

    private lateinit var geckoView: GeckoView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var title: TextView
    private lateinit var adBlocker: AdBlocker

    private val sessions = ArrayList<GeckoSession>()
    private var current = -1
    private var container = "personal"

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContentView(R.layout.activity_main)

        geckoView = findViewById(R.id.gecko)
        urlBar = findViewById(R.id.url)
        progress = findViewById(R.id.progress)
        title = findViewById(R.id.title)
        adBlocker = AdBlocker(this)

        findViewById<Button>(R.id.btn_back).setOnClickListener { cur()?.goBack() }
        findViewById<Button>(R.id.btn_fwd).setOnClickListener { cur()?.goForward() }
        findViewById<Button>(R.id.btn_reload).setOnClickListener { cur()?.reload() }
        findViewById<Button>(R.id.btn_home).setOnClickListener { cur()?.loadUri(HOME) }
        findViewById<Button>(R.id.btn_new_tab).setOnClickListener { newSession(HOME) }
        findViewById<Button>(R.id.btn_container).setOnClickListener { nextContainer() }

        urlBar.setOnEditorActionListener { _, actionId, e ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (e != null && e.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                navigate(urlBar.text.toString()); true
            } else false
        }

        newSession(HOME)
    }

    private fun buildSession(contextId: String): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .contextId(contextId)
            .build()
        val session = GeckoSession(settings)
        session.open(GeckoManager.get(this))
        session.setNavigationDelegate(navDelegate)
        session.setProgressDelegate(progressDelegate)
        session.setContentDelegate(contentDelegate)
        return session
    }

    private fun newSession(uri: String) {
        val session = buildSession(container)
        sessions.add(session)
        show(sessions.size - 1)
        session.loadUri(uri)
    }

    private fun nextContainer() {
        container = when (container) {
            "personal" -> "work"
            "work" -> "banking"
            else -> "personal"
        }
        val session = buildSession(container)
        sessions.add(session)
        show(sessions.size - 1)
        session.loadUri(HOME)
        Toast.makeText(this, "Container: $container (isolated storage)", Toast.LENGTH_SHORT).show()
    }

    private fun show(i: Int) {
        if (i !in sessions.indices) return
        current = i
        geckoView.setSession(sessions[i])
    }

    private fun cur(): GeckoSession? = if (current in sessions.indices) sessions[current] else null

    private fun navigate(input: String) {
        var u = input.trim()
        if (u.isEmpty()) return
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = if (u.contains(".") && !u.contains(" ")) "https://$u" else SEARCH + Uri.encode(u)
        }
        cur()?.loadUri(u)
        val im = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        im?.hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    private val navDelegate = object : GeckoSession.NavigationDelegate {
        override fun onLoadRequest(
            session: GeckoSession,
            request: GeckoSession.NavigationDelegate.LoadRequest
        ): GeckoResult<AllowOrDeny> {
            return if (adBlocker.isBlocked(request.uri)) GeckoResult.deny() else GeckoResult.allow()
        }
    }

    private val progressDelegate = object : GeckoSession.ProgressDelegate {
        override fun onProgressChange(session: GeckoSession, p: Int) {
            if (session === cur()) progress.progress = p
        }

        override fun onPageStart(session: GeckoSession, url: String) {
            if (session === cur()) urlBar.setText(url)
        }
    }

    private val contentDelegate = object : GeckoSession.ContentDelegate {
        override fun onTitleChange(session: GeckoSession, t: String?) {
            if (session === cur()) title.text = t ?: ""
        }
    }

    override fun onDestroy() {
        for (s in sessions) {
            try { s.close() } catch (_: Exception) { }
        }
        sessions.clear()
        super.onDestroy()
    }

    companion object {
        const val HOME = "https://duckduckgo.com/"
        const val SEARCH = "https://duckduckgo.com/"
    }
}
