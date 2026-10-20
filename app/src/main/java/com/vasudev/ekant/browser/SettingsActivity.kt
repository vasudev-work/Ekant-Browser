package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class SettingsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.settings_title

    private val seNames = arrayOf("Google", "DuckDuckGo", "Bing")
    private val seUrls = arrayOf(
        "https://www.google.com/search?q=",
        "https://duckduckgo.com/?q=",
        "https://www.bing.com/search?q="
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.sec_general))
        val g = b.card()
        b.valueRow(g, getString(R.string.r_search), engineName()) { pickEngine() }
        b.divider(g)
        b.valueRow(g, getString(R.string.r_home), homeLabel()) { pickHome() }
        b.divider(g)
        b.navRow(g, getString(R.string.r_ntp), null) { startActivity(Intent(this, NewTabPageActivity::class.java)) }
        b.divider(g)
        b.navRow(g, getString(R.string.r_downloads), null) {
            try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) } catch (_: Exception) { }
        }
        b.divider(g)
        b.switchRow(g, getString(R.string.r_close), null, prefs.getBoolean("close_on_last", false)) { _, on ->
            prefs.edit().putBoolean("close_on_last", on).apply()
        }

        b.section(getString(R.string.sec_display))
        val d = b.card()
        b.navRow(d, getString(R.string.r_appearance), null) { go(AppearanceActivity::class.java) }
        b.divider(d)
        b.navRow(d, getString(R.string.r_languages), null) { go(LanguagesActivity::class.java) }

        b.section(getString(R.string.sec_ai))
        val ai = b.card()
        b.iconRow(ai, R.drawable.ic_ai, getString(R.string.ai_title), getString(R.string.r_ai_sum)) {
            go(AIActivity::class.java)
        }

        b.section(getString(R.string.sec_passwords))
        val p = b.card()
        b.navRow(p, getString(R.string.r_passwords), null) { go(PasswordsActivity::class.java) }
        b.divider(p)
        b.navRow(p, getString(R.string.r_autofill), null) { go(AutofillActivity::class.java) }

        b.section(getString(R.string.sec_privacy))
        val s = b.card()
        b.navRow(s, getString(R.string.r_shields), getString(R.string.r_shields_sum)) {
            go(ShieldsActivity::class.java)
        }
        b.divider(s)
        b.iconSwitchRow(s, R.drawable.ic_fingerprint, getString(R.string.lock_private),
            getString(R.string.lock_private_sum), prefs.getBoolean("lock_private", false)) { _, on ->
            prefs.edit().putBoolean("lock_private", on).apply()
        }
        b.divider(s)
        b.navRow(s, getString(R.string.r_clear), getString(R.string.r_clear_sum)) { clearData() }

        b.section(getString(R.string.advanced_settings))
        val adv = b.card()
        b.navRow(adv, getString(R.string.advanced_settings),
            getString(R.string.advanced_settings_sum)) {
            startActivity(Intent(this, com.vasudev.Appstore.SettingsUIActivity::class.java))
        }

        b.section(getString(R.string.sec_privacy_data))
        val pd = b.card()
        b.navRow(pd, getString(R.string.r_privacy_dashboard), getString(R.string.r_privacy_dashboard_sum)) {
            go(PrivacyActivity::class.java)
        }
        b.divider(pd)
        b.navRow(pd, getString(R.string.r_home_shortcuts), getString(R.string.r_home_shortcuts_sum)) {
            go(HomeShortcutsActivity::class.java)
        }
        b.divider(pd)
        b.navRow(pd, getString(R.string.r_backup), getString(R.string.r_backup_sum)) {
            go(BackupActivity::class.java)
        }

        b.section(getString(R.string.sec_power))
        val pw = b.card()
        b.navRow(pw, getString(R.string.r_power), getString(R.string.r_power_sum)) {
            go(AdvancedActivity::class.java)
        }

        b.section(getString(R.string.sec_about))
        val ab = b.card()
        b.navRow(ab, getString(R.string.r_about), null) { go(AboutActivity::class.java) }
        b.divider(ab)
        b.switchRow(ab, "Show engine-only features",
            "Display Chromium-only options greyed out", prefs.getBoolean("show_engine", false)) { _, on ->
            prefs.edit().putBoolean("show_engine", on).apply()
            recreate()
        }

        if (prefs.getBoolean("show_engine", false)) {
            b.section("Features")
            b.note("These options need a custom Chromium engine build. They are shown for reference only.")
            val f = b.card()
            b.disabledRow(f, "News", "Personalised news feed"); b.divider(f)
            b.disabledRow(f, "Wallet", "Crypto and web3 wallet"); b.divider(f)
            b.disabledRow(f, "Firewall & VPN", "Network-level protection"); b.divider(f)
            b.disabledRow(f, "Sync", null); b.divider(f)
            b.disabledRow(f, "Privacy report", null); b.divider(f)
            b.disabledRow(f, "Secure DNS", null)
        }
    }

    private fun go(cls: Class<*>) { startActivity(Intent(this, cls)) }

    private fun engineName(): String {
        val se = prefs.getString("se", seUrls[0])
        for (i in seUrls.indices) if (seUrls[i] == se) return seNames[i]
        return seNames[0]
    }

    private fun homeLabel(): String {
        val h = prefs.getString("home", "") ?: ""
        return if (h.startsWith("http")) h else "New tab page"
    }

    private fun pickEngine() {
        AlertDialog.Builder(this).setTitle(R.string.r_search)
            .setItems(seNames) { _, which ->
                prefs.edit().putString("se", seUrls[which]).apply()
                recreate()
            }.show()
    }

    private fun pickHome() {
        val inEdit = EditText(this)
        inEdit.hint = "https://"
        val h = prefs.getString("home", "") ?: ""
        if (h.startsWith("http")) inEdit.setText(h)
        AlertDialog.Builder(this).setTitle(R.string.r_home).setView(inEdit)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.edit().putString("home", inEdit.text.toString().trim()).apply()
                recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearData() {
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        WebView(this).clearCache(true)
        prefs.edit().remove("history").remove("bookmarks").apply()
        Toast.makeText(this, R.string.t_cleared, Toast.LENGTH_SHORT).show()
    }
}
