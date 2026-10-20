package com.vasudev.ekant.gecko

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

/**
 * Aekant GeckoView build - Mozilla's non-Chromium Gecko engine.
 * Tabs, containers (real per-context isolation), ad-blocking, dark UI.
 */
class MainActivity : Activity() {

    private lateinit var geckoView: GeckoView
    private lateinit var urlBar: EditText
    private lateinit var titleView: TextView
    private lateinit var progress: ProgressBar
    private lateinit var tabCount: TextView
    private lateinit var adBlocker: AdBlocker
    private lateinit var prefs: SharedPreferences

    private val sessions = ArrayList<GeckoSession>()
    private val titles = ArrayList<String>()
    private val owners = ArrayList<String>()
    private var current = -1
    private var container = "personal"
    private var canBack = false
    private var canForward = false

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("aekant_gecko", MODE_PRIVATE)
        container = prefs.getString("container", "personal") ?: "personal"

        geckoView = findViewById(R.id.gecko)
        urlBar = findViewById(R.id.url)
        titleView = findViewById(R.id.title)
        progress = findViewById(R.id.progress)
        tabCount = findViewById(R.id.tab_count)

        adBlocker = AdBlocker(this)
        adBlocker.enabled = prefs.getBoolean("adblock", true)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { if (canBack) cur()?.goBack() }
        findViewById<ImageButton>(R.id.btn_fwd).setOnClickListener { if (canForward) cur()?.goForward() }
        findViewById<ImageButton>(R.id.btn_reload).setOnClickListener { cur()?.reload() }
        findViewById<ImageButton>(R.id.btn_home).setOnClickListener { cur()?.loadUri(HOME) }
        findViewById<ImageButton>(R.id.btn_tabs).setOnClickListener { showTabs() }
        findViewById<ImageButton>(R.id.btn_menu).setOnClickListener { showMenu() }

        urlBar.setOnEditorActionListener { _, actionId, e ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (e != null && e.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                navigate(urlBar.text.toString()); true
            } else false
        }

        newTab(HOME)
    }

    // ---------------- tabs ----------------
    private fun buildSession(contextId: String): GeckoSession {
        val settings = GeckoSessionSettings.Builder().contextId(contextId).build()
        val s = GeckoSession(settings)
        s.open(GeckoManager.get(this))
        s.setNavigationDelegate(navDelegate)
        s.setProgressDelegate(progressDelegate)
        s.setContentDelegate(contentDelegate)
        return s
    }

    private fun newTab(uri: String) {
        val s = buildSession(container)
        sessions.add(s); titles.add("New tab"); owners.add(container)
        show(sessions.size - 1)
        s.loadUri(uri)
        updateChrome()
    }

    private fun show(i: Int) {
        if (i !in sessions.indices) return
        current = i
        geckoView.setSession(sessions[i])
        canBack = false; canForward = false
        titleView.text = titles[i]
        updateChrome()
    }

    private fun closeTab(i: Int) {
        if (i !in sessions.indices) return
        val s = sessions.removeAt(i); titles.removeAt(i); owners.removeAt(i)
        try { s.close() } catch (_: Exception) { }
        if (sessions.isEmpty()) { newTab(HOME); return }
        show(if (i >= sessions.size) sessions.size - 1 else i)
    }

    private fun cur(): GeckoSession? = if (current in sessions.indices) sessions[current] else null

    private fun updateChrome() {
        tabCount.text = sessions.size.toString()
    }

    // ---------------- navigation ----------------
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

    // ---------------- delegates ----------------
    private val navDelegate = object : GeckoSession.NavigationDelegate {
        override fun onLoadRequest(
            session: GeckoSession,
            request: GeckoSession.NavigationDelegate.LoadRequest
        ): GeckoResult<AllowOrDeny> {
            return if (adBlocker.isBlocked(request.uri)) GeckoResult.deny() else GeckoResult.allow()
        }

        override fun onCanGoBack(session: GeckoSession, value: Boolean) {
            if (session === cur()) canBack = value
        }

        override fun onCanGoForward(session: GeckoSession, value: Boolean) {
            if (session === cur()) canForward = value
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
            val i = sessions.indexOfFirst { it === session }
            if (i >= 0) titles[i] = t ?: "Untitled"
            if (session === cur()) titleView.text = titles.getOrNull(i) ?: ""
        }
    }

    // ---------------- UI ----------------
    private fun showTabs() {
        val items = titles.mapIndexed { i, t ->
            (if (i == current) "\u2713 " else "") + (i + 1) + ". " + t + "  [" + owners[i] + "]"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tabs (" + sessions.size + ")")
            .setItems(items) { _, which -> show(which) }
            .setPositiveButton("New tab") { _, _ -> newTab(HOME) }
            .setNegativeButton("Close tab") { _, _ -> closeTab(current) }
            .show()
    }

    private fun showMenu() {
        val adState = if (adBlocker.enabled) "On" else "Off"
        val items = arrayOf(
            "New tab",
            "New container tab",
            "Block ads: $adState",
            "Engine: GeckoView (non-Chromium)",
            "About this build"
        )
        AlertDialog.Builder(this)
            .setTitle("Aekant Gecko")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> newTab(HOME)
                    1 -> showContainers()
                    2 -> {
                        adBlocker.enabled = !adBlocker.enabled
                        prefs.edit().putBoolean("adblock", adBlocker.enabled).apply()
                        toast("Ad blocking " + (if (adBlocker.enabled) "on" else "off"))
                        cur()?.reload()
                    }
                    3 -> AlertDialog.Builder(this).setTitle("Engine")
                        .setMessage("Rendering engine: Mozilla GeckoView (Gecko), non-Chromium.\n" +
                            "Tabs: " + sessions.size + "\nContainers are isolated by GeckoSessionSettings.contextId.")
                        .setPositiveButton("OK", null).show()
                    4 -> AlertDialog.Builder(this).setTitle("About")
                        .setMessage("Aekant - GeckoView spike build.\nDeveloper: Vasudev Danganya\n" +
                            "This is a proof-of-concept of the non-Chromium engine.")
                        .setPositiveButton("OK", null).show()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showContainers() {
        val list = containerList()
        val items = list.map { c -> (if (c == container) "\u2713 " else "") + c }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Containers (isolated storage)")
            .setItems(items) { _, which -> switchContainer(list[which]) }
            .setPositiveButton("New container") { _, _ -> newContainerDialog() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun containerList(): List<String> {
        val custom = prefs.getString("custom_containers", "") ?: ""
        val extra = custom.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return (listOf("personal", "work", "banking", "social") + extra).distinct()
    }

    private fun switchContainer(c: String) {
        container = c
        prefs.edit().putString("container", c).apply()
        newTab(HOME)
        toast("Container: $c (isolated cookies/cache)")
    }

    private fun newContainerDialog() {
        val inEdit = EditText(this)
        inEdit.hint = "Container name"
        inEdit.setPadding(48, 24, 48, 24)
        AlertDialog.Builder(this)
            .setTitle("New container")
            .setView(inEdit)
            .setPositiveButton("Create") { _, _ ->
                val name = inEdit.text.toString().trim().lowercase().replace(" ", "-")
                if (name.isEmpty()) return@setPositiveButton
                val cur0 = prefs.getString("custom_containers", "") ?: ""
                val next = if (cur0.isEmpty()) name else cur0 + "," + name
                prefs.edit().putString("custom_containers", next).apply()
                switchContainer(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (canBack) { cur()?.goBack(); return }
        if (sessions.size > 1) { closeTab(current); return }
        super.onBackPressed()
    }

    override fun onDestroy() {
        for (s in sessions) { try { s.close() } catch (_: Exception) { } }
        sessions.clear()
        super.onDestroy()
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()

    companion object {
        const val HOME = "https://duckduckgo.com/"
        const val SEARCH = "https://duckduckgo.com/?q="
    }
}
