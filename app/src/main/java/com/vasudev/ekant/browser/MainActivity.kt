package com.vasudev.ekant.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayInputStream
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    companion object {
        const val PREFS = "aekant"
        const val HOME_ASSET = "file:///android_asset/ntp.html"
        const val UA_DESKTOP =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        const val REQ_UNLOCK = 42
        val SE_NAMES = arrayOf("Google", "DuckDuckGo", "Bing")
        val SE_URLS = arrayOf(
            "https://www.google.com/search?q=",
            "https://duckduckgo.com/?q=",
            "https://www.bing.com/search?q="
        )
    }

    private lateinit var container: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var lock: ImageView
    private lateinit var containerDot: View
    private lateinit var containerName: TextView
    private lateinit var prefs: SharedPreferences

    private lateinit var overlay: View
    private lateinit var scrim: View
    private lateinit var panel: LinearLayout
    private lateinit var quick: LinearLayout
    private lateinit var menuList: LinearLayout
    private var menuBuilt = false
    private var menuOpen = false

    private var dark = true
    private var desktop = false
    private var incognito = false
    private var js = true
    private var cookies = true
    private var images = true
    private var closeOnLast = false
    private var ntpTopSites = true
    private var ntpStats = true
    private var ntpBg = true
    private var textZoom = 100
    private var searchBase = SE_URLS[0]
    private var pendingUnlock: Runnable? = null

    private lateinit var adBlocker: AdBlocker
    private lateinit var store: ContainerStore
    private var containers: MutableList<Container> = mutableListOf()
    private var active: Container? = null

    private val tabs = mutableListOf<WebView>()
    private var current = -1

    override fun onCreate(saved: Bundle?) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        readPrefs()
        setTheme(if (dark) R.style.Theme_Aekant else R.style.Theme_Aekant_Light)
        super.onCreate(saved)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        urlBar = findViewById(R.id.url)
        progress = findViewById(R.id.progress)
        lock = findViewById(R.id.lock)
        containerDot = findViewById(R.id.container_dot)
        containerName = findViewById(R.id.container_name)

        overlay = findViewById(R.id.overlay)
        scrim = findViewById(R.id.scrim)
        panel = findViewById(R.id.panel)
        quick = findViewById(R.id.quick)
        menuList = findViewById(R.id.menu_list)
        if (!dark) panel.setBackgroundResource(R.drawable.panel_bg_light)

        adBlocker = AdBlocker(applicationContext)
        store = ContainerStore(this)
        containers = store.getContainers().toMutableList()
        val aid = store.activeContainerId ?: containers.first().id
        active = containers.firstOrNull { it.id == aid } ?: containers.first()

        scrim.setOnClickListener { hideMenuPanel() }
        findViewById<View>(R.id.container_row).setOnClickListener { showContainers() }

        urlBar.setOnEditorActionListener { _, actionId, e ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (e != null && e.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                go(urlBar.text.toString()); true
            } else false
        }

        val homeClick = View.OnClickListener { goHome() }
        findViewById<View>(R.id.btn_top_home).setOnClickListener(homeClick)
        findViewById<View>(R.id.btn_home).setOnClickListener(homeClick)
        findViewById<View>(R.id.btn_refresh).setOnClickListener { cur()?.reload() }
        findViewById<View>(R.id.btn_back).setOnClickListener { cur()?.let { if (it.canGoBack()) it.goBack() } }
        findViewById<View>(R.id.btn_fwd).setOnClickListener { cur()?.let { if (it.canGoForward()) it.goForward() } }
        findViewById<View>(R.id.btn_tabs).setOnClickListener { showTabs() }
        findViewById<View>(R.id.btn_menu).setOnClickListener { toggleMenu() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (menuOpen) { hideMenuPanel(); return }
                val v = cur()
                if (v != null && v.canGoBack()) v.goBack()
                else if (tabs.size > 1) closeTab(current)
                else { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
            }
        })

        loadContainer(active!!)
    }

    private fun readPrefs() {
        dark = prefs.getBoolean("dark", true)
        desktop = prefs.getBoolean("desktop", false)
        js = prefs.getBoolean("js", true)
        cookies = prefs.getBoolean("cookies", true)
        images = prefs.getBoolean("images", true)
        closeOnLast = prefs.getBoolean("close_on_last", false)
        ntpTopSites = prefs.getBoolean("ntp_topsites", true)
        ntpStats = prefs.getBoolean("ntp_stats", true)
        ntpBg = prefs.getBoolean("ntp_bg", true)
        textZoom = prefs.getInt("zoom", 100)
        searchBase = prefs.getString("se", SE_URLS[0]) ?: SE_URLS[0]
    }

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    private fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()

    override fun onResume() {
        super.onResume()
        val wasDark = dark
        readPrefs()
        adBlocker.blockAds = prefs.getBoolean("block_ads", true)
        adBlocker.blockTrackers = prefs.getBoolean("block_trackers", true)
        CookieManager.getInstance().setAcceptCookie(cookies)
        if (wasDark != dark) { recreate(); return }
        for (t in tabs) applyContent(t.settings)
        updateContainerUi()
    }

    override fun onPause() {
        saveTabs()
        super.onPause()
    }

    override fun onDestroy() {
        for (t in tabs) t.destroy()
        tabs.clear()
        super.onDestroy()
    }

    // ---------- containers ----------
    private fun loadContainer(c: Container) {
        active = c
        store.activeContainerId = c.id
        for (t in tabs) { container.removeView(t); t.destroy() }
        tabs.clear()
        current = -1
        val saved = store.getTabs(c.id)
        if (saved.isEmpty()) {
            newTab(ntpUrl())
        } else {
            for (s in saved) newTab(s.url, select = false)
            showTab(0)
        }
        updateContainerUi()
    }

    private fun saveTabs() {
        val c = active ?: return
        store.saveTabs(c.id, tabs.map { TabState(it.url ?: ntpUrl(), it.title ?: "") })
    }

    private fun updateContainerUi() {
        val c = active ?: return
        containerName.text = c.name
        val d = GradientDrawable()
        d.shape = GradientDrawable.OVAL
        try { d.setColor(Color.parseColor(c.color)) } catch (_: Exception) { }
        containerDot.background = d
    }

    private fun showContainers() {
        val s = Sheet.build(this, getString(R.string.containers))
        for (i in containers.indices) {
            val c = containers[i]
            val mark = if (c.id == active?.id) "\u2713 " else ""
            s.item(mark + c.name, i) { switchContainer(containers[it]) }
        }
        s.action(getString(R.string.new_container)) { promptNewContainer() }
        s.action(getString(R.string.delete_current_container)) { deleteCurrentContainer() }
        s.show()
    }

    private fun switchContainer(c: Container) {
        if (c.id == active?.id) return
        saveTabs()
        loadContainer(c)
    }

    private fun promptNewContainer() {
        val inEdit = EditText(this)
        inEdit.hint = getString(R.string.container_name)
        inEdit.setPadding(dp(48), dp(24), dp(48), dp(24))
        AlertDialog.Builder(this).setTitle(R.string.new_container).setView(inEdit)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = inEdit.text.toString().trim()
                if (name.isNotEmpty()) {
                    val c = store.newContainer(name, store.palette[containers.size % store.palette.size])
                    containers.add(c)
                    switchContainer(c)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteCurrentContainer() {
        val c = active ?: return
        if (containers.size <= 1) { toast(getString(R.string.need_one_container)); return }
        store.removeContainer(c.id)
        containers.removeAll { it.id == c.id }
        loadContainer(containers.first())
    }

    // ---------- urls / content ----------
    private fun ntpUrl(): String =
        HOME_ASSET + "?topsites=" + (if (ntpTopSites) "1" else "0") +
            "&stats=" + (if (ntpStats) "1" else "0") +
            "&bg=" + (if (ntpBg) "1" else "0")

    private fun goHome() {
        val home = prefs.getString("home", "") ?: ""
        val target = if (home.startsWith("http")) home else ntpUrl()
        val w = cur()
        if (w != null) w.loadUrl(target) else newTab(target)
    }

    private fun applyContent(s: WebSettings) {
        s.javaScriptEnabled = js
        s.blockNetworkImage = !images
        s.textZoom = textZoom
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun newTab(u: String, select: Boolean = true): WebView {
        val w = WebView(this)
        w.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val s = w.settings
        s.domStorageEnabled = true
        s.setSupportZoom(true)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.loadWithOverviewMode = true
        s.useWideViewPort = true
        s.textZoom = textZoom
        s.mediaPlaybackRequiresUserGesture = true
        applyContent(s)
        applyUa(s)
        val block3p = prefs.getBoolean("block_3p", true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(w, cookies && !block3p)
        w.webViewClient = BrowserClient()
        w.setDownloadListener(DownloadListener { url, _, cd, mime, _ ->
            try {
                val name = URLUtil.guessFileName(url, cd, mime)
                val r = DownloadManager.Request(Uri.parse(url))
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(r)
                toast("Downloading " + name)
            } catch (_: Exception) {
                toast("Download failed")
            }
        })
        w.setFindListener { _, count, done -> if (done) toast("" + count + " matches") }
        w.visibility = View.GONE
        container.addView(w)
        tabs.add(w)
        if (select) showTab(tabs.size - 1)
        if (u.isNotEmpty()) w.loadUrl(u)
        return w
    }

    private fun applyUa(s: WebSettings) {
        s.userAgentString = if (desktop) UA_DESKTOP else null
    }

    private fun cur(): WebView? {
        if (current < 0 || current >= tabs.size) return null
        return tabs[current]
    }

    private fun showTab(i: Int) {
        for (k in tabs.indices) tabs[k].visibility = if (k == i) View.VISIBLE else View.GONE
        current = i
        updateUrl()
    }

    private fun closeTab(i: Int) {
        if (i < 0 || i >= tabs.size) return
        val w = tabs.removeAt(i)
        container.removeView(w)
        w.destroy()
        if (tabs.isEmpty()) {
            if (closeOnLast) finishAffinity() else newTab(ntpUrl())
        } else {
            showTab(Math.max(0, Math.min(i, tabs.size - 1)))
        }
    }

    private fun updateUrl() {
        val w = cur() ?: return
        val u = w.url
        if (u != null) {
            urlBar.setText(if (u.startsWith(HOME_ASSET)) "" else u)
            updateLock(u)
        }
    }

    private fun updateLock(url: String?) {
        val secure = url != null && url.startsWith("https")
        lock.setColorFilter(
            if (secure) resources.getColor(R.color.brand_green) else attr(R.attr.mutedColor))
    }

    private fun go(input: String?) {
        var u = input?.trim() ?: ""
        if (u.isEmpty()) return
        if (u.startsWith("aekant://")) { handleScheme(u); return }
        if (!u.startsWith("http://") && !u.startsWith("https://") && !u.startsWith("file:")) {
            u = if (u.contains(" ") || !u.contains(".")) searchBase + Uri.encode(u) else "https://$u"
        }
        cur()?.loadUrl(u)
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        im?.hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    private fun handleScheme(u: String) {
        val uri = Uri.parse(u)
        when (uri.host) {
            "search" -> go(searchBase + Uri.encode(uri.getQueryParameter("q")))
            "open" -> go(uri.getQueryParameter("u"))
        }
    }

    private inner class BrowserClient : WebViewClient() {
        override fun onPageStarted(v: WebView?, url: String?, fav: android.graphics.Bitmap?) {
            if (v == cur()) progress.visibility = View.VISIBLE
        }

        override fun onPageFinished(v: WebView?, url: String?) {
            if (v == cur()) {
                progress.visibility = View.GONE
                updateUrl()
            }
            if (!incognito && url != null && url.startsWith("http")) {
                addEntry("history", v?.title, url, 300, false)
            }
        }

        override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.startsWith("aekant://")) { handleScheme(url); return true }
            return false
        }

        override fun shouldInterceptRequest(v: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return null
            if (adBlocker.isBlocked(url)) {
                return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
            }
            return null
        }
    }

    // ---------- biometric lock ----------
    private fun openIncognito() {
        if (prefs.getBoolean("lock_private", false)) {
            pendingUnlock = Runnable { doOpenIncognito() }
            authenticate()
        } else {
            doOpenIncognito()
        }
    }

    private fun doOpenIncognito() {
        incognito = true
        newTab(ntpUrl())
        toast(getString(R.string.t_incognito))
    }

    private fun authenticate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val ex: Executor = mainExecutor
            val prompt = android.hardware.biometrics.BiometricPrompt.Builder(this)
                .setTitle(getString(R.string.lock_prompt))
                .setSubtitle(getString(R.string.lock_prompt_sub))
                .setNegativeButton(getString(R.string.d_close), ex) { _, _ -> }
                .build()
            prompt.authenticate(CancellationSignal(), ex,
                object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?
                    ) {
                        runOnUiThread { runPending() }
                    }
                })
        } else {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val i = km.createConfirmDeviceCredentialIntent(
                getString(R.string.lock_prompt), getString(R.string.lock_prompt_sub))
            if (i != null) { startActivityForResult(i, REQ_UNLOCK); return }
            runPending()
        }
    }

    private fun runPending() {
        pendingUnlock?.run()
        pendingUnlock = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_UNLOCK && resultCode == RESULT_OK) runPending()
    }

    // ---------- sliding menu ----------
    private fun toggleMenu() { if (menuOpen) hideMenuPanel() else showMenuPanel() }

    private fun showMenuPanel() {
        if (!menuBuilt) buildMenu()
        val h = (resources.displayMetrics.heightPixels * 0.72).toInt()
        panel.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM)
        overlay.visibility = View.VISIBLE
        scrim.alpha = 0f
        scrim.animate().alpha(1f).setDuration(200).start()
        panel.translationY = h.toFloat()
        panel.animate().translationY(0f).setDuration(220).start()
        menuOpen = true
    }

    private fun hideMenuPanel() {
        if (!menuOpen) return
        menuOpen = false
        panel.animate().translationY(panel.height.toFloat()).setDuration(180)
            .withEndAction { overlay.visibility = View.GONE }.start()
        scrim.animate().alpha(0f).setDuration(180).start()
    }

    private fun quickAction(iconRes: Int, label: String, l: View.OnClickListener): View {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.gravity = Gravity.CENTER_HORIZONTAL
        c.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        val ic = ImageView(this)
        ic.setImageResource(iconRes)
        ic.setColorFilter(attr(R.attr.textPrimaryColor))
        val sz = dp(24)
        ic.layoutParams = LinearLayout.LayoutParams(sz, sz)
        c.addView(ic)
        val t = TextView(this)
        t.text = label
        t.textSize = 11f
        t.setTextColor(attr(R.attr.textSecondaryColor))
        t.setPadding(0, dp(7), 0, 0)
        c.addView(t)
        c.setOnClickListener(l)
        c.isClickable = true
        return c
    }

    private fun buildMenu() {
        menuBuilt = true
        quick.removeAllViews()
        quick.addView(quickAction(R.drawable.ic_forward, getString(R.string.q_forward)) {
            cur()?.let { if (it.canGoForward()) it.goForward() }; hideMenuPanel() })
        quick.addView(quickAction(R.drawable.ic_star, getString(R.string.q_bookmark)) {
            addCurrentBookmark(); hideMenuPanel() })
        quick.addView(quickAction(R.drawable.ic_download, getString(R.string.q_download)) {
            openDownloads(); hideMenuPanel() })
        quick.addView(quickAction(R.drawable.ic_info, getString(R.string.q_info)) {
            showPageInfo() })
        quick.addView(quickAction(R.drawable.ic_refresh, getString(R.string.q_refresh)) {
            cur()?.reload(); hideMenuPanel() })

        val b = SettingsBuilder(this, menuList)

        b.section(getString(R.string.sec_tabs))
        val c1 = b.card()
        b.iconRow(c1, R.drawable.ic_new_tab, getString(R.string.m_new_tab), null, act(0)); b.divider(c1)
        b.iconRow(c1, R.drawable.ic_incognito, getString(R.string.m_new_incognito), null, act(1)); b.divider(c1)
        b.iconRow(c1, R.drawable.ic_tabs, getString(R.string.m_recent), null, act(2)); b.divider(c1)
        b.iconRow(c1, R.drawable.ic_ext, getString(R.string.m_containers), active?.name, act(3))

        b.section(getString(R.string.sec_library))
        val c2 = b.card()
        b.iconRow(c2, R.drawable.ic_history, getString(R.string.m_history), null, act(4)); b.divider(c2)
        b.iconRow(c2, R.drawable.ic_bookmark, getString(R.string.m_bookmarks), null, act(5)); b.divider(c2)
        b.iconRow(c2, R.drawable.ic_download, getString(R.string.m_downloads), null, act(6))

        b.section(getString(R.string.sec_tools))
        val c3 = b.card()
        b.iconRow(c3, R.drawable.ic_find, getString(R.string.m_find), null, act(7)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_share, getString(R.string.m_share), null, act(8)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_translate, getString(R.string.m_translate), null, act(9)); b.divider(c3)
        b.iconValueRow(c3, R.drawable.ic_desktop, getString(R.string.m_desktop),
            if (desktop) "On" else "Off", act(10)); b.divider(c3)
        b.iconValueRow(c3, R.drawable.ic_zoom, getString(R.string.m_zoom), "$textZoom%", act(11))

        b.section(getString(R.string.sec_more))
        val c4 = b.card()
        b.iconRow(c4, R.drawable.ic_ai, getString(R.string.ai_title), null, act(12)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_ext, getString(R.string.m_extensions), null, act(13)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_settings, getString(R.string.m_settings), null, act(14)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_info, getString(R.string.m_about), null, act(15)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_exit, getString(R.string.m_exit), null, act(16))
    }

    private fun act(which: Int): View.OnClickListener = View.OnClickListener { onMenuAction(which) }

    private fun onMenuAction(which: Int) {
        when (which) {
            0 -> { newTab(ntpUrl()); hideMenuPanel() }
            1 -> { hideMenuPanel(); openIncognito() }
            2 -> { hideMenuPanel(); showTabs() }
            3 -> { hideMenuPanel(); showContainers() }
            4 -> { hideMenuPanel(); showHistory() }
            5 -> { hideMenuPanel(); showBookmarks() }
            6 -> { openDownloads(); hideMenuPanel() }
            7 -> { hideMenuPanel(); showFind() }
            8 -> { sharePage(); hideMenuPanel() }
            9 -> { translatePage(); hideMenuPanel() }
            10 -> toggleDesktop()
            11 -> showZoom()
            12 -> { startActivity(Intent(this, AIActivity::class.java)); hideMenuPanel() }
            13 -> { hideMenuPanel(); showExtensions() }
            14 -> { startActivity(Intent(this, SettingsActivity::class.java)); hideMenuPanel() }
            15 -> { startActivity(Intent(this, AboutActivity::class.java)); hideMenuPanel() }
            16 -> finishAffinity()
        }
    }

    private fun rebuildMenu() {
        menuBuilt = false
        menuList.removeAllViews()
        quick.removeAllViews()
        buildMenu()
    }

    private fun toggleDesktop() {
        desktop = !desktop
        prefs.edit().putBoolean("desktop", desktop).apply()
        for (t in tabs) { applyUa(t.settings); t.reload() }
        toast(getString(if (desktop) R.string.t_desktop_on else R.string.t_desktop_off))
        rebuildMenu()
    }

    private fun showZoom() {
        Sheet.build(this, getString(R.string.m_zoom))
            .item("Zoom in", 0) { setZoom(textZoom + 10); rebuildMenu() }
            .item("Zoom out", 1) { setZoom(textZoom - 10); rebuildMenu() }
            .item("Reset zoom", 2) { setZoom(100); rebuildMenu() }
            .show()
    }

    private fun setZoom(z0: Int) {
        var z = z0
        if (z < 50) z = 50
        if (z > 200) z = 200
        textZoom = z
        prefs.edit().putInt("zoom", z).apply()
        cur()?.settings?.textZoom = z
    }

    private fun showPageInfo() {
        val v = cur() ?: return
        val t = TextView(this)
        val title = v.title ?: ""
        val url = v.url ?: ""
        t.text = "$title\n\n$url"
        t.setTextColor(attr(R.attr.textSecondaryColor))
        t.textSize = 14f
        t.setPadding(dp(20), dp(4), dp(20), dp(12))
        Sheet.build(this, getString(R.string.q_info)).content(t)
            .action(getString(R.string.d_close), null).show()
    }

    private fun addCurrentBookmark() {
        val v = cur() ?: return
        val u = v.url ?: return
        addEntry("bookmarks", v.title, u, 200, true)
        toast(getString(R.string.t_bookmarked))
    }

    private fun openDownloads() {
        try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) }
        catch (_: Exception) { toast("Downloads") }
    }

    // ---------- storage (local only) ----------
    private fun addEntry(key: String, title: String?, url: String?, cap: Int, unique: Boolean) {
        if (url == null) return
        val t = if (title.isNullOrEmpty()) url else title.replace("\n", " ")
        val line = t + "\u0001" + url
        val c0 = prefs.getString(key, "") ?: ""
        val l = ArrayList<String>()
        if (c0.isNotEmpty()) l.addAll(c0.split("\n"))
        if (unique && l.contains(line)) return
        l.add(0, line)
        while (l.size > cap) l.removeAt(l.size - 1)
        prefs.edit().putString(key, TextUtils.join("\n", l)).apply()
    }

    private fun entries(key: String): Array<String> {
        val s = prefs.getString(key, "") ?: ""
        if (s.isEmpty()) return emptyArray()
        return s.split("\n").toTypedArray()
    }

    private fun openEntry(e: String) {
        val i = e.indexOf('\u0001')
        if (i >= 0) go(e.substring(i + 1))
    }

    // ---------- sheets ----------
    private fun showHistory() {
        val a = entries("history")
        if (a.isEmpty()) { toast(getString(R.string.d_empty)); return }
        val s = Sheet.build(this, getString(R.string.d_history))
        for (i in a.indices) {
            val parts = a[i].split("\u0001")
            val url = a[i]
            s.item(if (parts.isNotEmpty()) parts[0] else a[i], i) { openEntry(url) }
        }
        s.action(getString(R.string.d_clear)) { prefs.edit().remove("history").apply() }
        s.show()
    }

    private fun showBookmarks() {
        val a = entries("bookmarks")
        if (a.isEmpty()) { toast(getString(R.string.d_empty)); return }
        val s = Sheet.build(this, getString(R.string.d_bookmarks))
        for (i in a.indices) {
            val parts = a[i].split("\u0001")
            val url = a[i]
            s.item(if (parts.isNotEmpty()) parts[0] else a[i], i) { openEntry(url) }
        }
        s.action(getString(R.string.d_add)) { addCurrentBookmark() }
        s.show()
    }

    private fun showTabs() {
        val s = Sheet.build(this, getString(R.string.d_recent))
        for (i in tabs.indices) {
            val t = tabs[i].title
            s.item(getString(R.string.d_tab) + " " + (i + 1) + ": " + (t ?: "New tab"), i) { showTab(it) }
        }
        s.action(getString(R.string.d_new_tab)) { newTab(ntpUrl()) }
        s.action(getString(R.string.d_close_tab)) { closeTab(current) }
        s.show()
    }

    private fun showFind() {
        val inEdit = EditText(this)
        inEdit.hint = getString(R.string.d_find_hint)
        inEdit.setPadding(dp(20), dp(10), dp(20), dp(10))
        Sheet.build(this, getString(R.string.m_find)).content(inEdit)
            .action(getString(R.string.d_find)) { cur()?.findAllAsync(inEdit.text.toString()) }
            .show()
    }

    private fun showExtensions() {
        val t = TextView(this)
        t.text = getString(R.string.d_ext_msg)
        t.setTextColor(attr(R.attr.textSecondaryColor))
        t.textSize = 14f
        t.setPadding(dp(20), dp(4), dp(20), dp(12))
        Sheet.build(this, getString(R.string.d_ext_title)).content(t)
            .action(getString(R.string.d_ext_open)) {
                val was = desktop
                desktop = true
                for (tt in tabs) applyUa(tt.settings)
                newTab("https://chromewebstore.google.com/")
                desktop = was
            }
            .action(getString(R.string.d_close), null)
            .show()
    }

    private fun sharePage() {
        val u = cur()?.url ?: return
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, u)
        startActivity(Intent.createChooser(i, getString(R.string.m_share)))
    }

    private fun translatePage() {
        val u = cur()?.url ?: return
        newTab("https://translate.google.com/translate?sl=auto&tl=en&u=" + Uri.encode(u))
    }

    private fun toast(m: String) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    }
}
