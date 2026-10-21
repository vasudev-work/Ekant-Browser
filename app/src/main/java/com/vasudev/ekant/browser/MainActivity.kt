package com.vasudev.ekant.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.webkit.GeolocationPermissions
import android.webkit.SslErrorHandler
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.ValueCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import android.webkit.PermissionRequest
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
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
        const val EXTRACT_JS = "(function(){try{var el=document.querySelector('article')||document.querySelector('main')||document.body;var t=(el&&el.innerText)||'';t=t.replace(/\\s+/g,' ').trim();return t.slice(0,200000);}catch(e){return '';}})()"

        const val READER_JS = """(function(){try{
function best(){try{var a=document.querySelector('article');if(a&&(a.innerText||'').length>400)return a;var m=document.querySelector('main');if(m&&(m.innerText||'').length>400)return m;var cands=[],ds=document.getElementsByTagName('div');for(var i=0;i<ds.length;i++){var tx=ds[i].innerText||'';if(tx.length<500)continue;var links=ds[i].getElementsByTagName('a').length;var score=tx.length/(1+links*40);cands.push([score,ds[i]]);}cands.sort(function(x,y){return y[0]-x[0];});return cands.length?cands[0][1]:(document.body||document.documentElement);}catch(e){return document.body;}}
var el=best();
try{var junk=el.querySelectorAll('nav,aside,footer,header,script,style,form,iframe,button,[role=navigation]');for(var i=0;i<junk.length;i++){if(junk[i].parentNode)junk[i].parentNode.removeChild(junk[i]);}}catch(e){}
var txt=el.innerText||'';var title=document.title||'';
function esc(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}
var css=':root{--bg:__BG__;--fg:__FG__;--br:__BR__}'
+'body{margin:0;background:var(--bg);color:var(--fg);font-family:Georgia,serif;line-height:1.75}'
+'.bar{position:sticky;top:0;display:flex;gap:8px;align-items:center;padding:10px 14px;background:var(--bg);border-bottom:1px solid rgba(128,128,128,.35);font-family:sans-serif}'
+'.bar button{background:transparent;border:1px solid rgba(128,128,128,.45);color:var(--fg);border-radius:8px;padding:5px 11px;font-size:14px}'
+'.sp{flex:1}h1{font-size:24px;color:var(--br);font-family:sans-serif;margin:14px 0 10px}'
+'p{font-size:17px;margin:0 0 16px}.wrap{max-width:760px;margin:0 auto;padding:8px 20px 70px}'
+'.wrap.vert{writing-mode:vertical-rl;text-orientation:mixed;height:76vh;overflow:auto;max-width:none;padding:8px 12px 8px 22px}'
+'body.sepia{background:#F4ECD8;color:#3B3226}body.dk{background:#0B0E12;color:#E8EAED}';
var bar='<div class="bar"><button onclick="fs(-1)">A-</button><button onclick="fs(1)">A+</button><span class="sp"></span><button onclick="th(0)">Light</button><button onclick="th(1)">Sepia</button><button onclick="th(2)">Dark</button><button onclick="location.reload()">Close</button></div>';
var js='<script>var f=17;function fs(d){f=Math.max(13,Math.min(28,f+d));var a=document.querySelectorAll("#c p");for(var i=0;i<a.length;i++){a[i].style.fontSize=f+"px";}}function th(m){document.body.className=(m===1?"sepia":(m===2?"dk":""));}<\/script>';
var h='<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"><style>'+css+'</style></head><body>'+bar+'<div class="wrap__VERTCLS__"><h1>'+esc(title)+'</h1><div id="c"></div></div>'+js+'</body></html>';
var ps=txt.split(/\s\s+/);var body='';
var BI=__BIONIC__;function bw(w){var n=Math.max(1,Math.ceil(w.length*0.4));return '<b>'+w.slice(0,n)+'</b>'+w.slice(n);}for(var i=0;i<ps.length;i++){var p=ps[i].trim();if(p.length>0){var e=esc(p);var x=BI?e.split(/(\s+)/).map(function(w){return /^\s+$/.test(w)?w:bw(w);}).join(''):e;body+='<p>'+x+'</p>';}}
h=h.replace('<div id="c"></div>','<div id="c">'+body+'</div>');
document.open();document.write(h);document.close();
}catch(e){}})();"""
        const val TOOLS_ASSET = "file:///android_asset/tools.html"

        const val PAGINATE_JS = "(function(){try{if(window.__aekantPaginate)return;window.__aekantPaginate=1;var n=0;" +
            "function nl(){var a=document.querySelector('a[rel=next]');if(a&&a.href)return a.href;" +
            "var as=document.querySelectorAll('a');for(var i=0;i<as.length;i++){var t=(as[i].innerText||'').trim().toLowerCase();" +
            "if(/^(next|next page|older|continue|next chapter|\u00bb|\u2192)$/.test(t))return as[i].href;}return null;}" +
            "function more(){if(n>=5)return;var u=nl();if(!u)return;n++;" +
            "fetch(u).then(function(r){return r.text();}).then(function(h){try{" +
            "var d=new DOMParser().parseFromString(h,'text/html');" +
            "var el=d.querySelector('article')||d.querySelector('main')||d.body;if(!el)return;" +
            "var holder=document.querySelector('article')||document.querySelector('main')||document.body;" +
            "var div=document.createElement('div');div.innerHTML=el.innerHTML;holder.appendChild(div);" +
            "setTimeout(more,600);}catch(e){}}).catch(function(){});}" +
            "window.addEventListener('scroll',function(){if(window.innerHeight+window.scrollY>document.body.scrollHeight-1200)more();},{passive:true});" +
            "}catch(e){}})()"

        const val LIVE_TEXT_JS = "(function(){try{if(window.__aekantMo)return;window.__aekantMo=1;var seen=new WeakSet();" +
            "function send(el){try{var t=(el.innerText||'').trim();if(t.length>40&&window.AekantMedia)window.AekantMedia.text(t);}catch(e){}}" +
            "var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){var a=ms[i].addedNodes;" +
            "for(var j=0;j<a.length;j++){var n=a[j];if(n.nodeType!==1)continue;" +
            "if(n.tagName==='P'&&!seen.has(n)){seen.add(n);send(n);}" +
            "var ps=n.querySelectorAll?n.querySelectorAll('p'):[];" +
            "for(var k=0;k<ps.length;k++){if(!seen.has(ps[k])){seen.add(ps[k]);send(ps[k]);}}}}});" +
            "mo.observe(document.body,{childList:true,subtree:true});}catch(e){}})()"

        const val REQ_UNLOCK = 42
        const val FP_SCRIPT = "(function(){try{var g=CanvasRenderingContext2D.prototype.getImageData;CanvasRenderingContext2D.prototype.getImageData=function(){var d=g.apply(this,arguments);try{for(var i=0;i<d.data.length;i+=997){d.data[i]=d.data[i]^1;}}catch(e){}return d;};}catch(e){}try{var t=HTMLCanvasElement.prototype.toDataURL;HTMLCanvasElement.prototype.toDataURL=function(){try{var c=this.getContext('2d');if(c){var d=c.getImageData(0,0,this.width,this.height);for(var i=0;i<d.data.length;i+=997){d.data[i]=d.data[i]^1;}c.putImageData(d,0,0);}}catch(e){}return t.apply(this,arguments);};}catch(e){}try{if(window.AudioBuffer){var p=AudioBuffer.prototype.getChannelData;AudioBuffer.prototype.getChannelData=function(){var a=p.apply(this,arguments);try{for(var i=0;i<a.length;i+=1000){a[i]=a[i]+(Math.random()-0.5)*1e-6;}}catch(e){}return a;};}}catch(e){}try{Object.defineProperty(navigator,'hardwareConcurrency',{get:function(){return 2+Math.floor(Math.random()*7);}});}catch(e){}try{Object.defineProperty(navigator,'deviceMemory',{get:function(){return [4,8][Math.floor(Math.random()*2)];}});}catch(e){}})();"
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
    private lateinit var panelContainerName: TextView
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
    private var memorySaver = true
    private var httpsOnly = true
    private var pendingUnlock: Runnable? = null

    private lateinit var adBlocker: AdBlocker
    private lateinit var store: ContainerStore
    private lateinit var extStore: ExtensionStore
    private lateinit var engineStore: SearchEngineStore
    private var containers: MutableList<Container> = mutableListOf()
    private var active: Container? = null

    private val tabs = mutableListOf<WebView>()
    private val favicons = HashMap<WebView, Bitmap>()
    private val pinned = HashSet<WebView>()
    private val closedStack = ArrayDeque<String>()
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val retries = HashMap<String, Int>()
    private val wrappers = HashMap<WebView, androidx.swiperefreshlayout.widget.SwipeRefreshLayout>()
    private var kiosk = false
    private var kioskHost = ""
    private var kioskExit: View? = null
    private var listenBar: LinearLayout? = null
    private var listenText: TextView? = null
    private var listenPlay: TextView? = null
    private var sleepBtn: TextView? = null
    private var mediaSession: android.media.session.MediaSession? = null
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null

    private val EXTERNAL_SCHEMES = setOf(
        "tel", "mailto", "sms", "smsto", "whatsapp", "geo", "market", "intent", "upi", "maps", "tg")
    private val INTERNAL_SCHEMES = setOf(
        "http", "https", "about", "data", "blob", "file", "javascript", "chrome", "aekant", "ws", "wss", "content")

    private val fileChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val cb = filePathCallback
        filePathCallback = null
        try { cb?.onReceiveValue(android.webkit.WebChromeClient.FileChooserParams.parseResult(res.resultCode, res.data)) } catch (_: Exception) { }
    }
    private var current = -1

    override fun onCreate(saved: Bundle?) {
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        engineStore = SearchEngineStore(this)
        readPrefs()
        setTheme(ThemeHelper.res(this))
        super.onCreate(saved)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        urlBar = findViewById(R.id.url)
        progress = findViewById(R.id.progress)
        lock = findViewById(R.id.lock)
        containerDot = findViewById(R.id.container_dot)
        containerName = findViewById(R.id.container_name)
        panelContainerName = findViewById(R.id.panel_container_name)

        overlay = findViewById(R.id.overlay)
        scrim = findViewById(R.id.scrim)
        panel = findViewById(R.id.panel)
        quick = findViewById(R.id.quick)
        menuList = findViewById(R.id.menu_list)
        if (!dark) panel.setBackgroundResource(R.drawable.panel_bg_light)

        WebView.setWebContentsDebuggingEnabled(prefs.getBoolean("remote_debug", false))
        AdFilterEngine.enabled = prefs.getBoolean("easylist", true)
        NetLog.enabled = prefs.getBoolean("net_log", false)
        AdFilterEngine.ensureLoaded(applicationContext)
        TtsReader.init(this)
        TtsReader.listener = object : TtsReader.Listener {
            override fun onState(index: Int, total: Int, sentence: String, speaking: Boolean) {
                listenText?.text = if (total == 0) "" else "" + (index + 1) + "/" + total + "  " + sentence
                listenPlay?.text = if (speaking) "\u23F8" else "\u25B6"
                if (total == 0) listenBar?.visibility = View.GONE
                if (speaking && sentence.isNotBlank()) highlightSentence(sentence)
            }
        }
        setupMediaSession()
        adBlocker = AdBlocker(applicationContext)
        store = ContainerStore(this)
        extStore = ExtensionStore(this)
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

        findViewById<View>(R.id.btn_refresh).setOnClickListener { cur()?.reload() }
        findViewById<View>(R.id.btn_back).setOnClickListener { cur()?.let { if (it.canGoBack()) it.goBack() } }
        findViewById<View>(R.id.btn_fwd).setOnClickListener { cur()?.let { if (it.canGoForward()) it.goForward() } }
        findViewById<View>(R.id.btn_menu).setOnClickListener { toggleMenu() }
        findViewById<View>(R.id.lock).setOnClickListener { showPrivacyPulse() }
        findViewById<View>(R.id.nav_home).setOnClickListener { goHome() }
        findViewById<View>(R.id.nav_bookmarks).setOnClickListener { showBookmarks() }
        findViewById<View>(R.id.nav_downloads).setOnClickListener { openDownloads() }
        findViewById<View>(R.id.nav_tabs).setOnClickListener { showTabs() }
        applyLayoutPrefs()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) { exitFullscreen(); return }
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
        dark = resolveDark()
        desktop = prefs.getBoolean("desktop", false)
        js = prefs.getBoolean("js", true)
        cookies = prefs.getBoolean("cookies", true)
        images = prefs.getBoolean("images", true)
        closeOnLast = prefs.getBoolean("close_on_last", false)
        ntpTopSites = prefs.getBoolean("ntp_topsites", true)
        ntpStats = prefs.getBoolean("ntp_stats", true)
        ntpBg = prefs.getBoolean("ntp_bg", true)
        textZoom = prefs.getInt("zoom", 100)
        searchBase = engineStore.default().template
        memorySaver = prefs.getBoolean("memory_saver", true)
        httpsOnly = prefs.getBoolean("https_only", true)
    }

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    private fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()

    /** Hide the system bars when immersive mode is on. */
    private fun applyImmersive() {
        val on = prefs.getBoolean("immersive", false)
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                val c = window.insetsController ?: return
                if (on) {
                    c.hide(android.view.WindowInsets.Type.systemBars())
                    c.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    c.show(android.view.WindowInsets.Type.systemBars())
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (on)
                    (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                else 0
            }
        } catch (_: Exception) { }
    }

    private fun applyKeepScreenOn() {
        try {
            if (prefs.getBoolean("keep_screen_on", false)) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } catch (_: Exception) { }
    }

    /** Save a picture of the current page. */
    private fun captureScreenshot() {
        val w = cur() ?: return
        try {
            val bmp = Bitmap.createBitmap(
                w.width.coerceAtLeast(1), w.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            w.draw(android.graphics.Canvas(bmp))
            val name = "aekant-" + System.currentTimeMillis() + ".png"
            if (Build.VERSION.SDK_INT >= 29) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Aekant")
                }
                val uri = contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    toast(getString(R.string.shot_saved))
                } else toast(getString(R.string.ctx_failed))
            } else {
                val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
                java.io.FileOutputStream(java.io.File(dir, name)).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                toast(getString(R.string.shot_saved))
            }
        } catch (_: Exception) {
            toast(getString(R.string.ctx_failed))
        }
    }

    /** Block screenshots when the user asks for it. */
    private fun applySecureScreen() {
        try {
            if (prefs.getBoolean("secure_screen", false)) {
                window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (_: Exception) { }
    }

    /** Kiosk mode: lock the browser to the current site, hide the chrome. */
    private fun enterKiosk() {
        val h = try { Uri.parse(cur()?.url ?: "").host ?: "" } catch (_: Exception) { "" }
        if (h.isEmpty()) { toast(getString(R.string.kiosk_need_page)); return }
        kiosk = true
        kioskHost = h
        try {
            findViewById<View>(R.id.topbar)?.visibility = View.GONE
            findViewById<View>(R.id.bottom_nav)?.visibility = View.GONE
        } catch (_: Exception) { }
        if (kioskExit == null) {
            val b = TextView(this)
            b.text = "\uD83D\uDD13"
            b.textSize = 18f
            b.setPadding(dp(10), dp(6), dp(10), dp(6))
            b.setBackgroundColor(attr(R.attr.surfaceColor))
            b.setOnClickListener { confirmExitKiosk() }
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.TOP or Gravity.END
            lp.topMargin = dp(10)
            lp.rightMargin = dp(10)
            try { findViewById<ViewGroup>(android.R.id.content).addView(b, lp) } catch (_: Exception) { }
            kioskExit = b
        }
        kioskExit?.visibility = View.VISIBLE
        toast(getString(R.string.kiosk_on, h))
    }

    private fun confirmExitKiosk() {
        AlertDialog.Builder(this)
            .setTitle(R.string.kiosk_title)
            .setMessage(R.string.kiosk_exit_q)
            .setPositiveButton(R.string.kiosk_exit) { _, _ -> exitKiosk() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun exitKiosk() {
        kiosk = false
        kioskHost = ""
        try {
            findViewById<View>(R.id.topbar)?.visibility = View.VISIBLE
            findViewById<View>(R.id.bottom_nav)?.visibility = View.VISIBLE
        } catch (_: Exception) { }
        kioskExit?.visibility = View.GONE
    }

    private fun applyOrientation() {
        try {
            requestedOrientation = when (prefs.getString("orientation", "system")) {
                "portrait" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                "landscape" -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } catch (_: Exception) { }
    }

    override fun onResume() {
        super.onResume()
        applyOrientation()
        applySecureScreen()
        applyImmersive()
        applyKeepScreenOn()
        val wasDark = dark
        readPrefs()
        adBlocker.blockAds = prefs.getBoolean("block_ads", true)
        adBlocker.blockTrackers = prefs.getBoolean("block_trackers", true)
        CookieManager.getInstance().setAcceptCookie(cookies)
        if (wasDark != dark) { recreate(); return }
        try { cur()?.onResume() } catch (_: Exception) { }
        applyLayoutPrefs()
        for (t in tabs) {
            applyContent(t.settings)
            try {
                t.isVerticalScrollBarEnabled = prefs.getBoolean("scrollbars", false)
                t.isHorizontalScrollBarEnabled = prefs.getBoolean("scrollbars", false)
            } catch (_: Exception) { }
        }
        updateContainerUi()
    }

    override fun onPause() {
        saveTabs()
        // With background playback on, do not pause the WebViews: that would stop the audio.
        if (!prefs.getBoolean("bg_media", false)) {
            for (t in tabs) { try { t.onPause() } catch (_: Exception) { } }
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (isFinishing && prefs.getBoolean("clear_on_exit", false)) clearEverything()
        for (t in tabs) t.destroy()
        tabs.clear()
        super.onDestroy()
    }

    /** Wipe cookies, storage, cache and history, e.g. on exit. */
    private fun clearEverything() {
        try {
            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        } catch (_: Exception) { }
        try { android.webkit.WebStorage.getInstance().deleteAllData() } catch (_: Exception) { }
        for (t in tabs) { try { t.clearCache(true) } catch (_: Exception) { } }
        try { prefs.edit().remove("history").apply() } catch (_: Exception) { }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (prefs.getBoolean("volume_scroll", false)) {
            val w = cur()
            if (w != null) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_DOWN -> { w.scrollBy(0, w.height / 2); return true }
                    KeyEvent.KEYCODE_VOLUME_UP -> { w.scrollBy(0, -(w.height / 2)); return true }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /** True when a regex URL filter matches. */
    private fun urlFiltered(url: String): Boolean {
        if (!prefs.getBoolean("url_filter_on", false)) return false
        val raw = prefs.getString("url_filters", "") ?: return false
        if (raw.isBlank()) return false
        for (line in raw.split("\n")) {
            val p = line.trim()
            if (p.isEmpty()) continue
            try { if (Regex(p, RegexOption.IGNORE_CASE).containsMatchIn(url)) return true } catch (_: Exception) { }
        }
        return false
    }

    // ---------- containers ----------
    private fun loadContainer(c: Container) {
        active = c
        store.activeContainerId = c.id
        for (t in tabs) { val wp = wrappers.remove(t); container.removeView(wp ?: t); t.destroy() }
        tabs.clear()
        current = -1
        val saved = if (prefs.getBoolean("restore_tabs", true)) store.getTabs(c.id) else emptyList()
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
        panelContainerName.text = c.name
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
    /** Offline toolbox page, themed like the rest of the app. */
    private fun toolsUrl(): String = TOOLS_ASSET +
        "?tbg=" + Uri.encode(hex(attr(R.attr.bgColor))) +
        "&tsurf=" + Uri.encode(hex(attr(R.attr.surfaceColor))) +
        "&tfg=" + Uri.encode(hex(attr(R.attr.textPrimaryColor))) +
        "&tmut=" + Uri.encode(hex(attr(R.attr.textSecondaryColor))) +
        "&tbrand=" + Uri.encode(hex(attr(R.attr.brandColor)))

    private fun ntpUrl(): String {
        val sc = prefs.getString("shortcuts", "") ?: ""
        val showRecent = prefs.getBoolean("show_recent", true)
        val rec = if (showRecent) recentForNtp() else ""
        return HOME_ASSET + "?topsites=" + (if (ntpTopSites) "1" else "0") +
            "&stats=" + (if (ntpStats) "1" else "0") +
            "&bg=" + (if (ntpBg) "1" else "0") +
            "&show_recent=" + (if (showRecent) "1" else "0") +
            "&style=" + (if (prefs.getBoolean("focus_mode", false)) "minimal"
                else (prefs.getString("start_style", "personal") ?: "personal")) +
            (if (sc.isNotBlank()) "&shortcuts=" + Uri.encode(sc) else "") +
            (if (rec.isNotBlank()) "&recent=" + Uri.encode(rec) else "") +
            "&greet=" + (if (prefs.getBoolean("greeting", true)) "1" else "0") +
            "&tbg=" + Uri.encode(hex(attr(R.attr.bgColor))) +
            "&tsurf=" + Uri.encode(hex(attr(R.attr.surfaceColor))) +
            "&tfg=" + Uri.encode(hex(attr(R.attr.textPrimaryColor))) +
            "&tmut=" + Uri.encode(hex(attr(R.attr.textSecondaryColor))) +
            "&tbrand=" + Uri.encode(hex(attr(R.attr.brandColor)))
    }

    private fun recentForNtp(): String {
        val a = entries("history")
        val out = ArrayList<String>()
        for (e in a) {
            if (out.size >= 6) break
            val p = e.split('\u0001')
            val title = p.getOrElse(0) { "" }.replace("|", " ")
            val url = p.getOrElse(1) { "" }
            if (url.startsWith("http")) out.add(title + "|" + url)
        }
        return out.joinToString(";;")
    }

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
        applyDarkMode(s)
    }

    /** Dark Reader style: let the engine darken sites that have no dark theme. */
    private fun applyDarkMode(s: WebSettings) {
        val on = prefs.getBoolean("force_dark", false)
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(s, on)
            } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(
                    s, if (on) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF)
            }
        } catch (_: Exception) { }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun newTab(u: String, select: Boolean = true): WebView {
        val w = AekantWebView(this)
        try { w.addJavascriptInterface(MediaBridge(), "AekantMedia") } catch (_: Exception) { }
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
        s.mediaPlaybackRequiresUserGesture = !prefs.getBoolean("bg_media", false)
        s.databaseEnabled = true
        s.allowFileAccess = false
        s.allowContentAccess = false
        s.allowFileAccessFromFileURLs = false
        s.allowUniversalAccessFromFileURLs = false
        s.setSupportMultipleWindows(true)
        s.javaScriptCanOpenWindowsAutomatically = prefs.getBoolean("popups", true)
        applyContent(s)
        applyUa(s)
        val block3p = prefs.getBoolean("block_3p", true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(w, cookies && !block3p)
        w.webViewClient = BrowserClient()
        w.webChromeClient = buildChromeClient()
        w.setDownloadListener(DownloadListener { url, _, cd, mime, _ ->
            val name = URLUtil.guessFileName(url, cd, mime)
            if (prefs.getBoolean("ask_download", false)) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.dl_confirm)
                    .setMessage(name)
                    .setPositiveButton(R.string.media_download) { _, _ -> startDownload(url, name, cd, mime) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                startDownload(url, name, cd, mime)
            }
        })
        w.setFindListener { _, count, done -> if (done) toast("" + count + " matches") }
        w.onTextAction = { action, text -> handleTextAction(action, text) }
        w.onLinkLongPress = { url, isImage -> showLinkMenu(url, isImage) }
        attachSwipe(w)
        val showBars = prefs.getBoolean("scrollbars", false)
        try { w.isVerticalScrollBarEnabled = showBars; w.isHorizontalScrollBarEnabled = showBars } catch (_: Exception) { }
        val wrapper = androidx.swiperefreshlayout.widget.SwipeRefreshLayout(this)
        wrapper.setOnRefreshListener { try { w.reload() } catch (_: Exception) { } }
        wrapper.isEnabled = prefs.getBoolean("pull_refresh", true)
        wrapper.addView(w, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        wrapper.visibility = View.GONE
        wrappers[w] = wrapper
        container.addView(wrapper)
        tabs.add(w)
        if (select) showTab(tabs.size - 1)
        if (u.isNotEmpty()) w.loadUrl(u)
        return w
    }

    private fun applyUa(s: WebSettings) {
        val custom = prefs.getString("ua_custom", "") ?: ""
        val desk = desktop || prefs.getBoolean("desktop_default", false)
        s.userAgentString = when {
            custom.isNotBlank() -> custom
            desk -> UA_DESKTOP
            else -> null
        }
    }

    private fun cur(): WebView? {
        if (current < 0 || current >= tabs.size) return null
        return tabs[current]
    }

    private fun showTab(i: Int) {
        val prev = current
        if (prev in tabs.indices && prev != i) {
            try { tabs[prev].onPause() } catch (_: Exception) { }
        }
        for (k in tabs.indices) (wrappers[tabs[k]] ?: tabs[k]).visibility = if (k == i) View.VISIBLE else View.GONE
        current = i
        if (prefs.getBoolean("memory_saver", true)) {
            try { tabs[i].onResume() } catch (_: Exception) { }
        }
        updateUrl()
    }

    private fun closeTab(i: Int) {
        if (i < 0 || i >= tabs.size) return
        val w = tabs.removeAt(i)
        val u = w.url
        if (u != null && u.startsWith("http")) {
            closedStack.addLast(u)
            if (closedStack.size > 10) closedStack.removeFirst()
        }
        if (prefs.getBoolean("clear_on_close", false) && u != null) clearSiteData(u)
        pinned.remove(w)
        favicons.remove(w)
        val wrap = wrappers.remove(w)
        if (wrap != null) container.removeView(wrap) else container.removeView(w)
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
            if (secure) attr(R.attr.brand2Color) else attr(R.attr.mutedColor))
    }

    /** Run every enabled extension whose pattern matches this URL. */
    private fun injectExtensions(wv: WebView?, url: String) {
        val w = wv ?: return
        for (e in extStore.getEnabled()) {
            if (e.matches(url)) {
                try { w.evaluateJavascript(e.code, null) } catch (_: Exception) { }
            }
        }
    }

    private var mediaPlaying = false

    /** Bridge the injected media listener calls into. */
    private inner class MediaBridge {
        @android.webkit.JavascriptInterface
        fun playing(on: Boolean) { runOnUiThread { onMediaState(on) } }

        @android.webkit.JavascriptInterface
        fun found(url: String, type: String) {
            try { MediaSniffer.add(url, type) } catch (_: Exception) { }
        }

        @android.webkit.JavascriptInterface
        fun text(t: String) {
            try { if (prefs.getBoolean("tts_live", true)) TtsReader.append(t) } catch (_: Exception) { }
        }
    }

    private fun onMediaState(on: Boolean) {
        if (!prefs.getBoolean("bg_media", false)) return
        if (on == mediaPlaying) return
        mediaPlaying = on
        if (on) MediaPlaybackService.start(this) else MediaPlaybackService.stop(this)
    }

    /** Learn when a page starts or stops playing media. */
    private fun injectMediaListener(wv: WebView?) {
        val w = wv ?: return
        val js = "(function(){if(window.__aekantMedia){return;}window.__aekantMedia=1;" +
            "function n(p){try{window.AekantMedia&&window.AekantMedia.playing(p);}catch(e){}}" +
            "['play','playing'].forEach(function(e){document.addEventListener(e,function(){n(true);},true);});" +
            "['pause','ended','emptied','abort'].forEach(function(e){document.addEventListener(e,function(){n(false);},true);});})();"
        try { w.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    /** Read the current page aloud. */
    private fun readAloud() {
        val w = cur() ?: return
        w.evaluateJavascript(EXTRACT_JS) { js ->
            val text = try { org.json.JSONTokener(js).nextValue() as? String ?: "" } catch (_: Exception) { "" }
            val parts = text.split(Regex("(?<=[.!?])\\s+"))
                .map { it.trim() }.filter { it.length > 1 }
            if (parts.isEmpty()) { toast(getString(R.string.tts_none)); return@evaluateJavascript }
            TtsReader.start(parts)
            showListenBar()
        }
    }

    private fun barButton(label: String, cb: () -> Unit): TextView {
        val t = TextView(this)
        t.text = label
        t.textSize = 16f
        t.setPadding(dp(12), dp(6), dp(12), dp(6))
        t.setTextColor(attr(R.attr.brandColor))
        t.setOnClickListener { cb() }
        return t
    }

    private fun showListenBar() {
        if (listenBar == null) {
            val bar = LinearLayout(this)
            bar.orientation = LinearLayout.HORIZONTAL
            bar.gravity = Gravity.CENTER_VERTICAL
            bar.setPadding(dp(10), dp(6), dp(10), dp(6))
            bar.setBackgroundColor(attr(R.attr.surfaceColor))
            listenText = TextView(this)
            listenText?.textSize = 12f
            listenText?.maxLines = 2
            listenText?.ellipsize = TextUtils.TruncateAt.END
            listenText?.setTextColor(attr(R.attr.textPrimaryColor))
            bar.addView(listenText, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            bar.addView(barButton("\u23EE") { TtsReader.prev() })
            listenPlay = barButton("\u23F8") { TtsReader.toggle() }
            bar.addView(listenPlay)
            bar.addView(barButton("\u23ED") { TtsReader.next() })
            bar.addView(barButton("1.0x") { cycleRate() })
            sleepBtn = barButton("\u23F1") { cycleSleep() }
            bar.addView(sleepBtn)
            val lp = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.BOTTOM
            lp.bottomMargin = dp(74)
            listenBar = bar
            try { findViewById<ViewGroup>(android.R.id.content).addView(bar, lp) } catch (_: Exception) { }
        }
        listenBar?.visibility = View.VISIBLE
    }

    private fun cycleSleep() {
        val cur = TtsReader.sleepMinutes()
        val next = when (cur) { 0 -> 15; 15 -> 30; 30 -> 60; else -> 0 }
        TtsReader.setSleep(next)
        sleepBtn?.text = if (next == 0) "\u23F1" else next.toString() + "m"
        toast(if (next == 0) getString(R.string.rd_sleep_off) else getString(R.string.rd_sleep_on, next))
    }

    private fun cycleRate() {
        val next = when {
            TtsReader.rate() < 1.05f -> 1.25f
            TtsReader.rate() < 1.3f -> 1.5f
            TtsReader.rate() < 1.55f -> 0.75f
            else -> 1.0f
        }
        TtsReader.setRate(next)
    }

    private fun setupMediaSession() {
        try {
            val ms = android.media.session.MediaSession(this, "Aekant")
            ms.setCallback(object : android.media.session.MediaSession.Callback() {
                override fun onPlay() { TtsReader.resume() }
                override fun onPause() { TtsReader.pause() }
                override fun onSkipToNext() { TtsReader.next() }
                override fun onSkipToPrevious() { TtsReader.prev() }
                override fun onStop() { TtsReader.stop() }
            })
            ms.isActive = true
            mediaSession = ms
        } catch (_: Exception) { }
    }

    /** HTML5 full-screen video: show the custom view over the whole window. */
    private fun enterFullscreen(view: View?, cb: WebChromeClient.CustomViewCallback?) {
        if (customView != null) { cb?.onCustomViewHidden(); return }
        customView = view
        customCallback = cb
        try {
            findViewById<ViewGroup>(android.R.id.content).addView(view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            findViewById<View>(R.id.column)?.visibility = View.GONE
        } catch (_: Exception) { }
    }

    private fun exitFullscreen() {
        val v = customView ?: return
        try {
            findViewById<ViewGroup>(android.R.id.content).removeView(v)
            findViewById<View>(R.id.column)?.visibility = View.VISIBLE
        } catch (_: Exception) { }
        customView = null
        customCallback?.onCustomViewHidden()
        customCallback = null
    }

    /** Drop cookies for the site a closed tab was showing. */
    private fun clearSiteData(url: String) {
        try {
            val cm = CookieManager.getInstance()
            val cookie = cm.getCookie(url) ?: return
            for (part in cookie.split(";")) {
                val name = part.substringBefore("=").trim()
                if (name.isEmpty()) continue
                cm.setCookie(url, name + "=; Max-Age=0; path=/")
            }
            cm.flush()
        } catch (_: Exception) { }
    }

    /** Highlight the sentence being read and scroll it into view. */
    private fun highlightSentence(sentence: String) {
        if (!prefs.getBoolean("highlight", true)) return
        val w = cur() ?: return
        val key = sentence.trim().take(48)
        if (key.length < 8) return
        val js = "(function(){try{var t=" + org.json.JSONObject.quote(key) + ";" +
            "var old=document.getElementById('aekant-hl');" +
            "if(old){var p=old.parentNode;p.replaceChild(document.createTextNode(old.textContent),old);p.normalize();}" +
            "var w=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,null,false),n;" +
            "while(n=w.nextNode()){var i=n.nodeValue.indexOf(t);if(i>=0){" +
            "var r=document.createRange();r.setStart(n,i);r.setEnd(n,i+t.length);" +
            "var m=document.createElement('mark');m.id='aekant-hl';" +
            "m.style.cssText='background:#ffe066;color:#000;border-radius:3px';" +
            "try{r.surroundContents(m);}catch(e){}" +
            "try{m.scrollIntoView({block:'center',behavior:'smooth'});}catch(e){}return;}}}" +
            "}catch(e){}})()"
        try { w.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    /** Auto-scroll and e-ink paper mode, when switched on. */
    private fun injectReadingHelpers(wv: WebView?) {
        val w = wv ?: return
        try {
            if (prefs.getBoolean("autoscroll", false)) {
                val px = prefs.getInt("autoscroll_px", 2)
                w.evaluateJavascript(
                    "(function(){if(window.__aekantScroll)return;window.__aekantScroll=1;" +
                        "setInterval(function(){window.scrollBy(0," + px + ");},80);})();", null)
            }
            if (prefs.getBoolean("paginate", false)) {
                w.evaluateJavascript(PAGINATE_JS, null)
            }
            if (prefs.getBoolean("tts_live", true)) {
                w.evaluateJavascript(LIVE_TEXT_JS, null)
            }
            val pf = prefs.getInt("page_font", 0)
            if (pf > 0 || prefs.getBoolean("amoled", false)) {
                val css = StringBuilder()
                if (pf > 0) css.append("body,p,li,span,div{font-size:").append(pf).append("px !important}")
                if (prefs.getBoolean("amoled", false)) css.append("html,body{background:#000 !important}")
                w.evaluateJavascript(
                    "try{var s=document.getElementById('aekant-page-style');" +
                        "if(!s){s=document.createElement('style');s.id='aekant-page-style';document.documentElement.appendChild(s);}" +
                        "s.textContent=" + org.json.JSONObject.quote(css.toString()) + ";}catch(e){}", null)
            }
            if (prefs.getBoolean("eink", false)) {
                w.evaluateJavascript(
                    "try{var s=document.getElementById('aekant-eink');" +
                        "if(!s){s=document.createElement('style');s.id='aekant-eink';document.documentElement.appendChild(s);}" +
                        "s.textContent='html{filter:grayscale(1) contrast(1.15) !important}';}catch(e){}", null)
            }
        } catch (_: Exception) { }
    }

    /** Stylus-like user CSS, applied per site pattern. */
    private fun injectUserStyles(wv: WebView?, url: String) {
        val w = wv ?: return
        val css = UserStyleStore.cssFor(this, url)
        if (css.isBlank()) return
        val js = "try{var s=document.getElementById('aekant-userstyle');" +
            "if(!s){s=document.createElement('style');s.id='aekant-userstyle';document.documentElement.appendChild(s);}" +
            "s.textContent=" + org.json.JSONObject.quote(css) + ";}catch(e){}"
        try { w.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    /** EasyList cosmetic rules: hide ad containers on the page. */
    private fun injectCosmetic(wv: WebView?, url: String) {
        val w = wv ?: return
        val host = try { Uri.parse(url).host ?: return } catch (_: Exception) { return }
        val css = AdFilterEngine.cosmeticCss(host)
        if (css.isEmpty()) return
        val js = "try{var s=document.getElementById('aekant-cosmetic');" +
            "if(!s){s=document.createElement('style');s.id='aekant-cosmetic';document.documentElement.appendChild(s);}" +
            "s.textContent=" + org.json.JSONObject.quote(css) + ";}catch(e){}"
        try { w.evaluateJavascript(js, null) } catch (_: Exception) { }
    }

    /** Best-effort canvas / audio fingerprint noise, injected on page load. */
    private fun injectProtection(wv: WebView?) {
        if (!prefs.getBoolean("fingerprint", true)) return
        val w = wv ?: return
        try { w.evaluateJavascript(FP_SCRIPT, null) } catch (_: Exception) { }
    }

    /** Edge-style horizontal swipe to go back / forward (opt-in). */
    @SuppressLint("ClickableViewAccessibility")
    private fun attachSwipe(w: WebView) {
        if (!prefs.getBoolean("swipe", true)) return
        var x0 = 0f; var y0 = 0f; var t0 = 0L
        w.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { x0 = e.x; y0 = e.y; t0 = System.currentTimeMillis() }
                MotionEvent.ACTION_UP -> {
                    val dx = e.x - x0
                    val dy = e.y - y0
                    val dt = System.currentTimeMillis() - t0
                    if (dt < 400 && Math.abs(dx) > dp(90) && Math.abs(dx) > 2 * Math.abs(dy)) {
                        if (dx > 0) { if (w.canGoBack()) w.goBack() }
                        else { if (w.canGoForward()) w.goForward() }
                    }
                }
            }
            false
        }
    }

    /** Save the current page through the Android print framework (Save as PDF). */
    private fun savePageAsPdf() {
        val w = cur() ?: return
        try {
            val pm = getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
            pm.print("Aekant page", w.createPrintDocumentAdapter("Aekant page"),
                android.print.PrintAttributes.Builder().build())
            toast(getString(R.string.t_pdf))
        } catch (_: Exception) {
            toast(getString(R.string.t_pdf_failed))
        }
    }

    /** Save the current page's HTML into the app's private files (offline copy). */
    private fun savePageOffline() {
        val w = cur() ?: return
        w.evaluateJavascript(
            "(function(){try{return document.documentElement.outerHTML;}catch(e){return '';}})()"
        ) { html ->
            try {
                val decoded = if (html == null) "" else
                    (org.json.JSONTokener(html).nextValue() as? String ?: "")
                if (decoded.isBlank()) { toast(getString(R.string.t_save_failed)); return@evaluateJavascript }
                val dir = java.io.File(filesDir, "saved")
                dir.mkdirs()
                val name = "page-" + System.currentTimeMillis() + ".html"
                java.io.File(dir, name).writeText(decoded)
                toast(getString(R.string.t_saved) + ": " + name)
            } catch (_: Exception) {
                toast(getString(R.string.t_save_failed))
            }
        }
    }

    private fun go(input: String?) {
        var u = input?.trim() ?: ""
        if (u.isEmpty()) return
        if (u.startsWith("aekant://")) { handleScheme(u); return }
        if (!u.startsWith("http://") && !u.startsWith("https://") && !u.startsWith("file:")) {
            u = if (u.contains(" ") || !u.contains(".")) searchFor(u) else "https://$u"
        }
        if (prefs.getBoolean("https_only", true) && u.startsWith("http://")) {
            u = "https://" + u.substring(7)
        }
        cur()?.loadUrl(u)
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        im?.hideSoftInputFromWindow(urlBar.windowToken, 0)
    }

    private fun handleScheme(u: String) {
        val uri = Uri.parse(u)
        when (uri.host) {
            "search" -> go(searchFor(uri.getQueryParameter("q") ?: ""))
            "open" -> go(uri.getQueryParameter("u"))
            "tools" -> go(toolsUrl())
        }
    }

    private fun buildChromeClient(): WebChromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            return try {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val tmp = AekantWebView(this@MainActivity)
                tmp.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView?, r: WebResourceRequest?): Boolean {
                        val u = r?.url?.toString() ?: return false
                        newTab(u)
                        return true
                    }
                }
                transport.webView = tmp
                resultMsg.sendToTarget()
                true
            } catch (_: Exception) { false }
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            enterFullscreen(view, callback)
        }

        override fun onHideCustomView() {
            exitFullscreen()
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            callback?.invoke(origin, !prefs.getBoolean("block_perms", true), false)
        }


        override fun onShowFileChooser(view: WebView?, cb: ValueCallback<Array<Uri>>?, params: FileChooserParams?): Boolean {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = cb
            return try {
                val i = params?.createIntent() ?: return false
                fileChooser.launch(i)
                true
            } catch (_: Exception) {
                filePathCallback = null
                false
            }
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            val r = request ?: return
            val host = try { Uri.parse(r.origin.toString()).host ?: "" } catch (_: Exception) { "" }
            val allowed = (prefs.getString("site_allow", "") ?: "")
                .split("\n").any { it.equals(host, true) }
            if (prefs.getBoolean("block_perms", true) && !allowed) r.deny() else r.grant(r.resources)
        }

        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            if (view != null && icon != null) favicons[view] = icon
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (view == cur()) {
                progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }
    }

    private inner class BrowserClient : WebViewClient() {
        override fun onPageStarted(v: WebView?, url: String?, fav: android.graphics.Bitmap?) {
            if (v == cur()) progress.visibility = View.VISIBLE
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            if (handler == null) return
            AlertDialog.Builder(this@MainActivity)
                .setTitle(R.string.ssl_title)
                .setMessage(getString(R.string.ssl_msg, error?.url ?: ""))
                .setCancelable(false)
                .setPositiveButton(R.string.ssl_back) { _, _ -> handler.cancel() }
                .setNegativeButton(R.string.ssl_proceed) { _, _ -> handler.proceed() }
                .show()
        }

        override fun onPageFinished(v: WebView?, url: String?) {
            try { wrappers[v]?.isRefreshing = false } catch (_: Exception) { }
            if (v == cur()) {
                progress.visibility = View.GONE
                updateUrl()
            }
            if (!incognito && url != null && url.startsWith("http")) {
                addEntry("history", v?.title, url, 300, false)
            }
            if (url != null) {
                injectProtection(v)
                injectExtensions(v, url)
                injectCosmetic(v, url)
                injectMediaListener(v)
                injectUserStyles(v, url)
                injectReadingHelpers(v)
            }
        }

        override fun onReceivedError(v: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
            if (request?.isForMainFrame == true) {
                val failed = request.url?.toString() ?: ""
                if (prefs.getBoolean("auto_retry", true) && failed.startsWith("http")) {
                    val n = retries.getOrDefault(failed, 0)
                    if (n < 2) {
                        retries[failed] = n + 1
                        toast(getString(R.string.retry_msg))
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try { v?.loadUrl(failed) } catch (_: Exception) { }
                        }, 2500)
                        return
                    }
                }
                val html = "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'>" +
                    "<style>body{background:" + hex(attr(R.attr.bgColor)) + ";color:" + hex(attr(R.attr.textPrimaryColor)) +
                    ";font-family:sans-serif;text-align:center;padding:56px 24px}" +
                    "h2{color:" + hex(attr(R.attr.brandColor)) + "}p{color:" + hex(attr(R.attr.textSecondaryColor)) +
                    ";font-size:14px}</style></head><body>" +
                    "<h2>Page not available</h2><p>" + (error?.description ?: "Could not load this page.") + "</p>" +
                    "<p>" + (request.url?.toString() ?: "") + "</p></body></html>"
                v?.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        }

        override fun shouldOverrideUrlLoading(v: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            if (url.startsWith("aekant://")) { handleScheme(url); return true }
            if (urlFiltered(url)) {
                val html = "<html><body style='background:" + hex(attr(R.attr.bgColor)) +
                    ";color:" + hex(attr(R.attr.textPrimaryColor)) + ";font-family:sans-serif;text-align:center;padding:60px 24px'>" +
                    "<h2 style='color:" + hex(attr(R.attr.brandColor)) + "'>Blocked</h2>" +
                    "<p>A filter rule blocks this address.</p><p style='font-size:12px'>" + url + "</p></body></html>"
                v?.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                return true
            }
            val scheme = Uri.parse(url).scheme?.lowercase() ?: ""
            if (EXTERNAL_SCHEMES.contains(scheme)) {
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true
                } catch (_: Exception) { true }
            }
            if (scheme.isNotEmpty() && !INTERNAL_SCHEMES.contains(scheme)) return true
            if (prefs.getBoolean("https_only", true) && url.startsWith("http://")) {
                v?.loadUrl("https://" + url.substring(7)); return true
            }
            if (kiosk) {
                val h = try { Uri.parse(url).host ?: "" } catch (_: Exception) { "" }
                if (h.isNotEmpty() && h != kioskHost) {
                    toast(getString(R.string.kiosk_locked))
                    return true
                }
            }
            if (prefs.getBoolean("external_links", false) && url.startsWith("http")) {
                val here = try { Uri.parse(cur()?.url ?: "").host ?: "" } catch (_: Exception) { "" }
                val there = try { Uri.parse(url).host ?: "" } catch (_: Exception) { "" }
                if (here.isNotEmpty() && there.isNotEmpty() && there != here) {
                    return try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); true }
                    catch (_: Exception) { false }
                }
            }
            if (prefs.getBoolean("clean_urls", true)) {
                val c = UrlCleaner.clean(url)
                if (c != url) { v?.loadUrl(c); return true }
            }
            return false
        }

        override fun shouldInterceptRequest(v: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            val url = request?.url?.toString() ?: return null
            try {
                if (request.isForMainFrame) MediaSniffer.reset(request.url.host ?: "")
                MediaSniffer.add(url, "")
            } catch (_: Exception) { }
            if (prefs.getBoolean("block_fonts", false)) {
                val path = url.substringBefore('?').lowercase()
                if (path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") || path.endsWith(".otf")) {
                    return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                }
            }
            if (urlFiltered(url)) {
                try { NetLog.add(url, true) } catch (_: Exception) { }
                return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
            }
            if (AdFilterEngine.isBlocked(url) || adBlocker.isBlocked(url)) {
                try { AdFilterEngine.recordBlocked() } catch (_: Exception) { }
                try { NetLog.add(url, true) } catch (_: Exception) { }
                return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
            }
            try { NetLog.add(url, false) } catch (_: Exception) { }
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
        b.iconValueRow(c3, R.drawable.ic_zoom, getString(R.string.m_zoom), "$textZoom%", act(11)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_download, getString(R.string.m_save_pdf), null, act(12)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_folder, getString(R.string.m_save_page), null, act(13)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_find, getString(R.string.m_reader), null, act(14)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_ai, getString(R.string.m_summary), null, act(15)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_star, getString(R.string.m_save_later), null, act(16)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_download, getString(R.string.m_media), null, act(17)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_ai, getString(R.string.m_listen), null, act(18)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_ext, getString(R.string.m_tools), null, act(19)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_lock, getString(R.string.kiosk_title), null, act(20)); b.divider(c3)
        b.iconRow(c3, R.drawable.ic_download, getString(R.string.m_screenshot), null, act(21))

        b.section(getString(R.string.sec_more))
        val c4 = b.card()
        b.iconRow(c4, R.drawable.ic_star, getString(R.string.m_pin), null, act(22)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_ai, getString(R.string.ai_title), null, act(23)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_ext, getString(R.string.m_extensions), null, act(24)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_settings, getString(R.string.m_settings), null, act(25)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_info, getString(R.string.m_about), null, act(26)); b.divider(c4)
        b.iconRow(c4, R.drawable.ic_exit, getString(R.string.m_exit), null, act(27))
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
            12 -> { savePageAsPdf(); hideMenuPanel() }
            13 -> { savePageOffline(); hideMenuPanel() }
            14 -> { readerMode(); hideMenuPanel() }
            15 -> { aiSummary(); hideMenuPanel() }
            16 -> { saveForLater(); hideMenuPanel() }
            17 -> { startActivity(Intent(this, MediaActivity::class.java)); hideMenuPanel() }
            18 -> { readAloud(); hideMenuPanel() }
            19 -> { go(toolsUrl()); hideMenuPanel() }
            20 -> { enterKiosk(); hideMenuPanel() }
            21 -> { captureScreenshot(); hideMenuPanel() }
            22 -> { addToHomeScreen(); hideMenuPanel() }
            23 -> { startActivity(Intent(this, AIActivity::class.java)); hideMenuPanel() }
            24 -> { startActivity(Intent(this, ExtensionsActivity::class.java)); hideMenuPanel() }
            25 -> { startActivity(Intent(this, SettingsActivity::class.java)); hideMenuPanel() }
            26 -> { startActivity(Intent(this, AboutActivity::class.java)); hideMenuPanel() }
            27 -> finishAffinity()
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
        val base = t + "\u0001" + url
        val line = base + "\u0001" + System.currentTimeMillis()
        val c0 = prefs.getString(key, "") ?: ""
        val l = ArrayList<String>()
        if (c0.isNotEmpty()) l.addAll(c0.split("\n"))
        if (unique && l.any { it.startsWith(base + "\u0001") }) return
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

    private fun resolveDark(): Boolean = when (prefs.getString("theme_mode", "dark")) {
        "light" -> false
        "system" -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        else -> true
    }

    /** Grid tab switcher: favicon + title + close, with New tab / Close all. */
    /** Tab switcher: search field, grid with favicons, and quick actions. */
    private fun showTabs() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        val pad = dp(12)
        root.setPadding(pad, pad, pad, pad)

        val search = EditText(this)
        search.hint = getString(R.string.tabs_search)
        search.isSingleLine = true
        search.setTextColor(attr(R.attr.textPrimaryColor))
        search.setHintTextColor(attr(R.attr.textSecondaryColor))
        val sbg = GradientDrawable()
        sbg.cornerRadius = dp(18).toFloat()
        sbg.setColor(attr(R.attr.surface2Color))
        search.background = sbg
        search.setPadding(dp(14), dp(10), dp(14), dp(10))
        root.addView(search)

        val grid = GridLayout(this)
        grid.columnCount = 3
        val scroll = ScrollView(this)
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val actions = LinearLayout(this)
        actions.orientation = LinearLayout.HORIZONTAL
        root.addView(actions)

        val dialog = AlertDialog.Builder(this)
            .setView(root)
            .setNegativeButton(R.string.d_close, null)
            .create()

        fun addAction(label: String, cb: () -> Unit) {
            val t = TextView(this)
            t.text = label
            t.textSize = 12f
            t.setTextColor(attr(R.attr.brandColor))
            t.setPadding(dp(12), dp(14), dp(12), dp(4))
            t.setOnClickListener { cb() }
            actions.addView(t)
        }

        fun buildGrid(filter: String) {
            grid.removeAllViews()
            for (i in tabs.indices) {
                val wv = tabs[i]
                val title = wv.title ?: "New tab"
                if (filter.isNotBlank() &&
                    !title.contains(filter, true) &&
                    !(wv.url ?: "").contains(filter, true)) continue

                val cell = LinearLayout(this)
                cell.orientation = LinearLayout.VERTICAL
                cell.gravity = Gravity.CENTER_HORIZONTAL
                val lp = GridLayout.LayoutParams()
                lp.width = 0
                lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                lp.setMargins(dp(4), dp(4), dp(4), dp(4))
                cell.layoutParams = lp
                cell.setPadding(dp(8), dp(10), dp(8), dp(10))
                val bg = GradientDrawable()
                bg.cornerRadius = dp(14).toFloat()
                bg.setColor(if (i == current) attr(R.attr.surface2Color) else attr(R.attr.surfaceColor))
                cell.background = bg

                val iv = ImageView(this)
                val fav = favicons[wv]
                if (fav != null) iv.setImageBitmap(fav) else iv.setImageResource(R.drawable.ic_tabs)
                iv.setColorFilter(attr(R.attr.textSecondaryColor))
                iv.layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
                cell.addView(iv)

                val tv = TextView(this)
                tv.text = if (pinned.contains(wv)) "\uD83D\uDCCC " + title else title
                tv.maxLines = 2
                tv.ellipsize = TextUtils.TruncateAt.END
                tv.gravity = Gravity.CENTER
                tv.textSize = 11f
                tv.setTextColor(attr(R.attr.textPrimaryColor))
                tv.setPadding(0, dp(6), 0, dp(2))
                cell.addView(tv)

                val close = TextView(this)
                close.text = "\u2715"
                close.textSize = 12f
                close.gravity = Gravity.CENTER
                close.setTextColor(attr(R.attr.brandColor))
                close.setPadding(dp(10), dp(2), dp(10), dp(2))
                close.setOnClickListener { closeTab(i); dialog.dismiss(); showTabs() }
                cell.addView(close)

                cell.setOnClickListener { showTab(i); dialog.dismiss() }
                grid.addView(cell)
            }
        }

        buildGrid("")
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { buildGrid(s?.toString() ?: "") }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
        })

        addAction(getString(R.string.d_new_tab)) { dialog.dismiss(); newTab(ntpUrl()) }
        addAction(getString(R.string.tabs_reopen)) { dialog.dismiss(); reopenClosed() }
        addAction(getString(R.string.tabs_duplicate)) { dialog.dismiss(); duplicateTab(current) }
        addAction(getString(R.string.tabs_pin)) { dialog.dismiss(); togglePin(current); showTabs() }
        addAction(getString(R.string.d_close_all_tabs)) { dialog.dismiss(); closeAllTabs() }

        dialog.show()
    }

    /** Save the current page into the reading list. */
    private fun saveForLater() {
        val w = cur() ?: return
        val url = w.url ?: return
        if (!url.startsWith("http")) { toast(getString(R.string.rl_none)); return }
        val title = (w.title ?: url).replace("|", " ")
        val line = title + '\u0001' + url + '\u0001' + "0"
        val cur0 = (prefs.getString("reading", "") ?: "")
        val lines = cur0.split("\n").filter { it.isNotBlank() && !it.endsWith('\u0001' + url + '\u0001' + "0") }
        val next = (lines + line).joinToString("\n")
        prefs.edit().putString("reading", next).apply()
        toast(getString(R.string.rl_saved))
    }

    private fun handleTextAction(action: String, text: String) {
        when (action) {
            "search" -> newTab(searchFor(text))
            "share" -> shareText(text)
            "translate" -> newTab("https://translate.google.com/?sl=auto&tl=en&text=" + Uri.encode(text))
            "ai" -> askAiAbout(text)
        }
    }

    private fun shareText(text: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, text)
        try { startActivity(Intent.createChooser(i, getString(R.string.ctx_share))) } catch (_: Exception) { }
    }

    private fun askAiAbout(text: String) {
        val endpoint = prefs.getString("ai_endpoint", "") ?: ""
        if (endpoint.isBlank()) { toast(getString(R.string.ai_setup)); return }
        toast(getString(R.string.sum_working))
        Thread {
            val answer = callAi(endpoint, prefs.getString("ai_key", "") ?: "",
                prefs.getString("ai_model", "gpt-4o-mini") ?: "", text)
            runOnUiThread {
                AlertDialog.Builder(this).setTitle(R.string.ctx_ask_ai).setMessage(answer)
                    .setPositiveButton(android.R.string.ok, null).show()
            }
        }.start()
    }

    /** Long-press menu for links and images. */
    private fun showLinkMenu(url: String, isImage: Boolean) {
        val actions = if (isImage)
            arrayOf(getString(R.string.ctx_open_new), getString(R.string.ctx_open_private),
                getString(R.string.ctx_copy), getString(R.string.ctx_share), getString(R.string.ctx_save_image))
        else
            arrayOf(getString(R.string.ctx_open_new), getString(R.string.ctx_open_private),
                getString(R.string.ctx_copy), getString(R.string.ctx_share))
        AlertDialog.Builder(this)
            .setTitle(url.take(60))
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> newTab(url)
                    1 -> { incognito = true; newTab(url) }
                    2 -> copyToClipboard(url)
                    3 -> shareText(url)
                    4 -> downloadUrl(url)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun copyToClipboard(text: String) {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("Aekant", text))
            toast(getString(R.string.ctx_copied))
        } catch (_: Exception) { }
    }

    /** Enqueue a download, honouring the background-download setting. */
    private fun startDownload(url: String, name: String, cd: String?, mime: String?) {
        try {
            val r = DownloadManager.Request(Uri.parse(url))
            r.setNotificationVisibility(
                if (prefs.getBoolean("bg_download", true))
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else DownloadManager.Request.VISIBILITY_HIDDEN)
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                if (name.isBlank()) URLUtil.guessFileName(url, cd, mime) else name)
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(r)
            toast(getString(R.string.ctx_downloading))
        } catch (_: Exception) {
            toast(getString(R.string.ctx_failed))
        }
    }

    private fun downloadUrl(url: String) {
        val name = URLUtil.guessFileName(url, null, null)
        if (prefs.getBoolean("ask_download", false)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.dl_confirm)
                .setMessage(name)
                .setPositiveButton(R.string.media_download) { _, _ -> startDownload(url, name, null, null) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            startDownload(url, name, null, null)
        }
    }

    private fun reopenClosed() {
        val u = closedStack.removeLastOrNull()
        if (u == null) { toast(getString(R.string.tabs_none_closed)); return }
        newTab(u)
    }

    private fun duplicateTab(i: Int) {
        val u = tabs.getOrNull(i)?.url
        newTab(if (u.isNullOrBlank()) ntpUrl() else u)
    }

    private fun togglePin(i: Int) {
        val w = tabs.getOrNull(i) ?: return
        if (pinned.contains(w)) { pinned.remove(w); toast(getString(R.string.tabs_unpinned)) }
        else { pinned.add(w); toast(getString(R.string.tabs_pinned)) }
    }

    private fun closeAllTabs() {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirm_close_all)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val keep = ArrayList<WebView>()
                for (t in ArrayList(tabs)) {
                    if (pinned.contains(t)) { keep.add(t); continue }
                    val wp = wrappers.remove(t); container.removeView(wp ?: t); t.destroy(); favicons.remove(t)
                }
                tabs.clear(); tabs.addAll(keep); current = -1
                if (tabs.isEmpty()) newTab(ntpUrl()) else showTab(0)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showFind() {
        val inEdit = EditText(this)
        inEdit.hint = getString(R.string.d_find_hint)
        inEdit.setPadding(dp(20), dp(10), dp(20), dp(10))
        Sheet.build(this, getString(R.string.m_find)).content(inEdit)
            .action(getString(R.string.d_find)) { cur()?.findAllAsync(inEdit.text.toString()) }
            .show()
    }

    private fun readerMode() {
        cur()?.evaluateJavascript(readerJs(), null)
    }

    private fun addToHomeScreen() {
        val w = cur() ?: return
        val url = w.url ?: return
        val title = (w.title ?: url)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            try {
                val sm = getSystemService(android.content.Context.SHORTCUT_SERVICE)
                    as android.content.pm.ShortcutManager
                if (sm.isRequestPinShortcutSupported) {
                    val sc = android.content.pm.ShortcutInfo.Builder(this, "pin-" + System.currentTimeMillis())
                        .setShortLabel(if (title.length > 20) title.substring(0, 20) else title)
                        .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        .build()
                    sm.requestPinShortcut(sc, null)
                    toast(getString(R.string.t_pin_requested))
                } else {
                    toast(getString(R.string.t_pin_unsupported))
                }
            } catch (_: Exception) {
                toast(getString(R.string.t_pin_unsupported))
            }
        } else {
            toast(getString(R.string.t_pin_unsupported))
        }
    }

    private fun aiSummary() {
        val w = cur() ?: return
        val endpoint = prefs.getString("ai_endpoint", "") ?: ""
        if (endpoint.isBlank()) { toast(getString(R.string.ai_setup)); return }
        w.evaluateJavascript("(function(){try{return document.body.innerText.slice(0,6000);}catch(e){return '';}})()") { js ->
            val text = try { org.json.JSONTokener(js).nextValue() as? String ?: "" } catch (_: Exception) { "" }
            if (text.isBlank()) { toast(getString(R.string.sum_none)); return@evaluateJavascript }
            toast(getString(R.string.sum_working))
            Thread {
                val summary = callAi(
                    endpoint,
                    prefs.getString("ai_key", "") ?: "",
                    prefs.getString("ai_model", "gpt-4o-mini") ?: "",
                    "Summarise this web page in 5 short bullet points:\n\n" + text
                )
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.m_summary)
                        .setMessage(summary)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }.start()
        }
    }

    private fun callAi(endpoint: String, key: String, model: String, prompt: String): String {
        var c: java.net.HttpURLConnection? = null
        return try {
            c = java.net.URL(endpoint).openConnection() as java.net.HttpURLConnection
            c.requestMethod = "POST"
            c.connectTimeout = 15000
            c.readTimeout = 30000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            if (key.isNotBlank()) c.setRequestProperty("Authorization", "Bearer " + key)
            val body = org.json.JSONObject()
            body.put("model", model)
            val msgs = org.json.JSONArray()
            val m = org.json.JSONObject()
            m.put("role", "user"); m.put("content", prompt); msgs.put(m)
            body.put("messages", msgs)
            c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = c.responseCode
            val r = java.io.BufferedReader(java.io.InputStreamReader(
                if (code >= 400) c.errorStream else c.inputStream, "UTF-8"))
            val sb = StringBuilder()
            var line = r.readLine()
            while (line != null) { sb.append(line); line = r.readLine() }
            r.close()
            if (code >= 400) getString(R.string.ai_error)
            else {
                val j = org.json.JSONObject(sb.toString())
                if (j.has("choices")) j.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content")
                else sb.toString()
            }
        } catch (_: Exception) {
            getString(R.string.ai_error)
        } finally {
            c?.disconnect()
        }
    }

    private fun searchFor(q: String): String = engineStore.urlFor(q)

    private fun hex(c: Int): String = String.format("#%06X", 0xFFFFFF and c)

    private fun readerJs(): String = READER_JS
        .replace("__BG__", hex(attr(R.attr.bgColor)))
        .replace("__FG__", hex(attr(R.attr.textPrimaryColor)))
        .replace("__BR__", hex(attr(R.attr.brandColor)))
        .replace("__BIONIC__", if (prefs.getBoolean("bionic", false)) "1" else "0")
        .replace("__VERTCLS__", if (prefs.getBoolean("vertical_reading", false)) " vert" else "")

    private fun onOff(v: Boolean) = if (v) "On" else "Off"

    /** Privacy Pulse: what is protecting this page right now. */
    private fun showPrivacyPulse() {
        val w = cur() ?: return
        val url = w.url ?: ""
        val secure = url.startsWith("https")
        val sb = StringBuilder()
        sb.append(if (secure) "Secure connection (HTTPS)" else "Not secure (HTTP)")
        sb.append("\n\nProtections on this device:")
        sb.append("\n\u2022 Ads: ").append(onOff(prefs.getBoolean("block_ads", true)))
        sb.append("\n\u2022 Trackers: ").append(onOff(prefs.getBoolean("block_trackers", true)))
        sb.append("\n\u2022 Third-party cookies: ")
            .append(if (prefs.getBoolean("block_3p", true)) "Blocked" else "Allowed")
        sb.append("\n\u2022 HTTPS-only: ").append(onOff(prefs.getBoolean("https_only", true)))
        sb.append("\n\u2022 Fingerprint noise: ").append(onOff(prefs.getBoolean("fingerprint", true)))
        sb.append("\n\u2022 Site permissions: ")
            .append(if (prefs.getBoolean("block_perms", true)) "Blocked" else "Asked")
        sb.append("\n\n").append(url)
        AlertDialog.Builder(this)
            .setTitle(R.string.pulse_title)
            .setMessage(sb.toString())
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /** Applies address-bar / navigation placement and compact mode. */
    private fun applyLayoutPrefs() {
        val col = findViewById<LinearLayout>(R.id.column)
        val topBar = findViewById<View>(R.id.topbar)
        val nav = findViewById<View>(R.id.bottom_nav)
        val containerRow = findViewById<View>(R.id.container_row)
        val prog = findViewById<View>(R.id.progress)
        val web = findViewById<View>(R.id.container)

        val addrBottom = prefs.getString("address_bar", "top") == "bottom"
        val navTop = prefs.getString("toolbar_pos", "bottom") == "top"
        val focus = prefs.getBoolean("focus_mode", false)
        containerRow.visibility = if (focus) View.GONE else View.VISIBLE

        nav.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(if (prefs.getBoolean("compact", false)) 46 else 58))

        col.removeAllViews()
        if (navTop) col.addView(nav)
        if (!addrBottom) col.addView(topBar)
        col.addView(containerRow)
        col.addView(prog)
        col.addView(web)
        if (addrBottom) col.addView(topBar)
        if (!navTop) col.addView(nav)
    }

    private fun sharePage() {
        var u = cur()?.url ?: return
        if (prefs.getBoolean("clean_urls", true)) u = UrlCleaner.clean(u)
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
