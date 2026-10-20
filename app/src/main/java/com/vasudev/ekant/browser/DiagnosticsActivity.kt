package com.vasudev.ekant.browser

import android.app.ActivityManager
import android.content.Context
import android.widget.LinearLayout
import androidx.webkit.WebViewCompat

/** Live state of the engine, filters and background services. */
class DiagnosticsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.dg_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.dg_title))
        val c = b.card()
        b.valueRow(c, getString(R.string.dg_webview), webViewVersion(), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.dg_memory), memory(), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.dg_service),
            if (MediaPlaybackService.running) getString(R.string.dg_on) else getString(R.string.dg_off), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.dg_tts),
            if (TtsReader.isSpeaking()) getString(R.string.dg_speaking)
            else if (TtsReader.isActive()) getString(R.string.dg_paused) else getString(R.string.dg_off), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.dg_rules), "" + AdFilterEngine.ruleCount(), null)

        b.divider(c)
        b.valueRow(c, getString(R.string.dg_crash), crashInfo(), null)

        b.section(getString(R.string.dg_data))
        val d = b.card()
        b.valueRow(d, getString(R.string.dg_history), "" + count("history"), null)
        b.divider(d)
        b.valueRow(d, getString(R.string.s_bookmarks), "" + count("bookmarks"), null)
        b.divider(d)
        b.valueRow(d, getString(R.string.rl_title), "" + count("reading"), null)
        b.divider(d)
        b.valueRow(d, getString(R.string.r_extensions), "" + ExtensionStore(this).getAll().size, null)
        b.divider(d)
        b.valueRow(d, getString(R.string.us_title), "" + UserStyleStore.all(this).size, null)
        b.note(getString(R.string.dg_note))
    }

    private fun crashInfo(): String = try {
        val f = java.io.File(filesDir, "crash.log")
        if (!f.exists() || f.length() == 0L) getString(R.string.dg_none)
        else java.text.SimpleDateFormat("d MMM HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(f.lastModified()))
    } catch (_: Exception) { getString(R.string.dg_none) }

    private fun count(key: String): Int {
        val s = prefs.getString(key, "") ?: ""
        if (s.isBlank()) return 0
        return s.split("\n").count { it.isNotBlank() }
    }

    private fun webViewVersion(): String = try {
        val p = WebViewCompat.getCurrentWebViewPackage(this)
        (p?.packageName ?: "WebView") + " " + (p?.versionName ?: "?")
    } catch (_: Exception) { "?" }

    private fun memory(): String = try {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()))
        val kb = info.firstOrNull()?.totalPss ?: 0
        (kb / 1024).toString() + " MB"
    } catch (_: Exception) { "?" }
}
