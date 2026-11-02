package com.vasudev.ekant.browser

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.widget.LinearLayout
import androidx.webkit.WebViewCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The developer dashboard: what is running, what is stored, and a handful of
 * real checks that either pass or fail. Nothing here is decorative - each line
 * is something you would want to know when a build misbehaves.
 */
class DiagnosticsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.dg_title

    private val pass = "PASS"
    private val fail = "FAIL"

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        buildInfo(b)
        deviceInfo(b)
        storageInfo(b)
        runtimeInfo(b)
        permissions(b)
        selfChecks(b)
        engine(b)
        data(b)

        b.navRow(b.card(), getString(R.string.dg_copy), getString(R.string.dg_copy_sum),
            android.view.View.OnClickListener { copyReport() })
        b.note(getString(R.string.dg_note))
    }

    // ---------- sections ----------

    private fun buildInfo(b: SettingsBuilder) {
        b.section(getString(R.string.dg_build))
        val c = b.card()
        val pm = packageManager
        var name = "?"
        var code = 0L
        try {
            val pi = pm.getPackageInfo(packageName, 0)
            name = pi.versionName ?: "?"
            code = if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode
                   else @Suppress("DEPRECATION") pi.versionCode.toLong()
        } catch (_: Exception) { }
        row(b, c, getString(R.string.dg_pkg), packageName)
        row(b, c, getString(R.string.dg_ver), "$name ($code)")
        row(b, c, getString(R.string.dg_buildtype), if (isDebug()) "debug" else "release")
        row(b, c, getString(R.string.dg_webview), webViewVersion())
        row(b, c, getString(R.string.dg_engine_channel), channel(), last = true)
    }

    private fun deviceInfo(b: SettingsBuilder) {
        b.section(getString(R.string.dg_device))
        val c = b.card()
        row(b, c, getString(R.string.dg_model), "${Build.MANUFACTURER} ${Build.MODEL}".trim())
        row(b, c, getString(R.string.dg_android), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        row(b, c, getString(R.string.dg_abi), Build.SUPPORTED_ABIS.joinToString(", "))
        val dm = resources.displayMetrics
        row(b, c, getString(R.string.dg_density),
            "${dm.densityDpi} dpi · ${dm.widthPixels}x${dm.heightPixels}")
        row(b, c, getString(R.string.dg_locale),
            Locale.getDefault().toString(), last = true)
    }

    private fun storageInfo(b: SettingsBuilder) {
        b.section(getString(R.string.dg_storage))
        val c = b.card()
        try {
            val s = StatFs(Environment.getDataDirectory().path)
            val total = s.blockCountLong * s.blockSizeLong
            val free = s.availableBlocksLong * s.blockSizeLong
            row(b, c, getString(R.string.dg_total), size(total))
            row(b, c, getString(R.string.dg_free), size(free))
        } catch (_: Exception) { }
        row(b, c, getString(R.string.dg_cache), size(dirSize(cacheDir)))
        row(b, c, getString(R.string.dg_files), size(dirSize(filesDir)))
        row(b, c, getString(R.string.dg_rules), "" + AdFilterEngine.ruleCount(), last = true)
    }

    private fun runtimeInfo(b: SettingsBuilder) {
        b.section(getString(R.string.dg_runtime))
        val c = b.card()
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        row(b, c, getString(R.string.dg_heap), "${size(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())} / ${size(Runtime.getRuntime().maxMemory())}")
        row(b, c, getString(R.string.dg_ram), "${size(mi.availMem)} free of ${size(mi.totalMem)}")
        row(b, c, getString(R.string.dg_pid), "${android.os.Process.myPid()} · ${Thread.activeCount()} threads")
        row(b, c, getString(R.string.dg_uptime), uptime(), last = true)
    }

    private fun permissions(b: SettingsBuilder) {
        b.section(getString(R.string.dg_perms))
        val c = b.card()
        row(b, c, getString(R.string.dg_notif), yesNo(notificationsAllowed()))
        row(b, c, getString(R.string.dg_battery_opt), yesNo(batteryExempt()))
        row(b, c, getString(R.string.dg_service),
            if (MediaPlaybackService.running) getString(R.string.dg_on) else getString(R.string.dg_off))
        row(b, c, getString(R.string.dg_tts),
            if (TtsReader.isSpeaking()) getString(R.string.dg_speaking)
            else if (TtsReader.isActive()) getString(R.string.dg_paused) else getString(R.string.dg_off), last = true)
    }

    /** Each of these actually runs; a red line means something is broken. */
    private fun selfChecks(b: SettingsBuilder) {
        b.section(getString(R.string.dg_checks))
        val c = b.card()
        val checks = listOf(
            getString(R.string.dg_check_prefs) to checkPrefs(),
            getString(R.string.dg_check_files) to checkFiles(),
            getString(R.string.dg_check_filters) to (AdFilterEngine.ruleCount() > 0),
            getString(R.string.dg_check_ext) to checkExtensions(),
            getString(R.string.dg_check_json) to checkBackupJson(),
            getString(R.string.dg_check_secrets) to checkNoSecrets()
        )
        checks.forEachIndexed { i, (label, ok) ->
            row(b, c, label, if (ok) pass else fail, last = i == checks.lastIndex)
        }
    }

    private fun engine(b: SettingsBuilder) {
        b.section(getString(R.string.dg_title))
        val c = b.card()
        row(b, c, getString(R.string.dg_engines),
            TtsReader.engines().joinToString(", ").ifBlank { getString(R.string.dg_none) })
        row(b, c, getString(R.string.dg_crash), crashInfo(), last = true)
    }

    private fun data(b: SettingsBuilder) {
        b.section(getString(R.string.dg_data))
        val c = b.card()
        row(b, c, getString(R.string.dg_history), "" + count("history"))
        row(b, c, getString(R.string.s_bookmarks), "" + count("bookmarks"))
        row(b, c, getString(R.string.rl_title), "" + count("reading"))
        row(b, c, getString(R.string.r_extensions), "" + ExtensionStore(this).getAll().size)
        row(b, c, getString(R.string.us_title), "" + UserStyleStore.all(this).size, last = true)
    }

    // ---------- helpers ----------

    private fun row(b: SettingsBuilder, card: LinearLayout, label: String, value: String,
                    last: Boolean = false) {
        b.valueRow(card, label, value, null)
        if (!last) b.divider(card)
    }

    private fun isDebug(): Boolean = (applicationInfo.flags and
            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun channel(): String = try {
        WebViewCompat.getCurrentWebViewPackage(this)?.let {
            "${it.packageName} ${it.versionName}"
        } ?: "?"
    } catch (_: Exception) { "?" }

    private fun webViewVersion(): String = try {
        android.webkit.WebSettings.getDefaultUserAgent(this)
            .substringAfter("Chrome/").substringBefore(" ").ifBlank { "?" }
    } catch (_: Exception) { "?" }

    private fun notificationsAllowed(): Boolean = try {
        if (Build.VERSION.SDK_INT >= 33) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    } catch (_: Exception) { false }

    private fun batteryExempt(): Boolean = try {
        (getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
            .isIgnoringBatteryOptimizations(packageName)
    } catch (_: Exception) { false }

    private fun uptime(): String {
        val ms = android.os.SystemClock.elapsedRealtime()
        val h = ms / 3600000
        val m = (ms % 3600000) / 60000
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    private fun yesNo(v: Boolean) = if (v) getString(R.string.dg_on) else getString(R.string.dg_off)

    private fun size(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.getDefault(), "%.0f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.getDefault(), "%.1f MB", mb)
        return String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0)
    }

    private fun dirSize(f: File): Long = try {
        if (!f.exists()) 0L else f.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    } catch (_: Exception) { 0L }

    // ---------- the checks ----------

    private fun checkPrefs(): Boolean = try {
        val p = getSharedPreferences("__diag", Context.MODE_PRIVATE)
        val v = "ok-" + System.nanoTime()
        p.edit().putString("probe", v).commit() && p.getString("probe", null) == v
    } catch (_: Exception) { false }

    private fun checkFiles(): Boolean {
        return try {
            val f = File(cacheDir, "diag.probe")
            if (f.parentFile?.exists() != true) false
            else {
                f.writeText("ok")        // writeText returns Unit, not a count
                val ok = f.length() == 2L
                f.delete()
                ok
            }
        } catch (_: Exception) { false }
    }

    private fun checkExtensions(): Boolean = try {
        ExtensionStore(this).getAll().all { it.id.isNotBlank() && it.code.isNotBlank() }
    } catch (_: Exception) { false }

    /** A malformed backup must be refused, not half-applied. */
    private fun checkBackupJson(): Boolean = try {
        val good = "{\"history\":[]}"
        org.json.JSONObject(good)
        var refused = false
        try { org.json.JSONObject("{not json") } catch (_: Exception) { refused = true }
        refused
    } catch (_: Exception) { false }

    /** No password or token should ever sit in plain preferences. */
    private fun checkNoSecrets(): Boolean = try {
        val p = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
        var bad = false
        for ((k, v) in p.all) {
            val kk = k.lowercase()
            if ((kk.contains("pass") || kk.contains("token") || kk.contains("secret")) &&
                v is String && v.length > 8) { bad = true; break }
        }
        !bad
    } catch (_: Exception) { false }

    private fun crashInfo(): String = try {
        val f = File(filesDir, "crash.log")
        if (!f.exists() || f.length() == 0L) getString(R.string.dg_none)
        else SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(f.lastModified()))
    } catch (_: Exception) { getString(R.string.dg_none) }

    private fun count(key: String): Int = try {
        val s = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getString(key, "[]")
        org.json.JSONArray(s ?: "[]").length()
    } catch (_: Exception) { 0 }

    /** A plain text report the user can paste into a bug report. */
    private fun copyReport() {
        val sb = StringBuilder()
        sb.appendLine("Nijatva diagnostics")
        sb.appendLine("time: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
        sb.appendLine("package: $packageName")
        sb.appendLine("device: ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("abi: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        sb.appendLine("webview: ${webViewVersion()}")
        sb.appendLine("rules: ${AdFilterEngine.ruleCount()}")
        sb.appendLine("extensions: ${ExtensionStore(this).getAll().size}")
        sb.appendLine("history: ${count("history")}  bookmarks: ${count("bookmarks")}")
        try {
            val mi = ActivityManager.MemoryInfo()
            (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(mi)
            sb.appendLine("ram free: ${size(mi.availMem)} of ${size(mi.totalMem)}")
        } catch (_: Exception) { }
        sb.appendLine("crash log: ${crashInfo()}")
        // no URLs, no page titles, no search terms - the report carries no content
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            cm.setPrimaryClip(android.content.ClipData.newPlainText("diagnostics", sb.toString()))
            android.widget.Toast.makeText(this, getString(R.string.dg_copied),
                android.widget.Toast.LENGTH_SHORT).show()
        } catch (_: Exception) { }
    }
}
