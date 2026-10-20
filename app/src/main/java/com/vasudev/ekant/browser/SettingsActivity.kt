package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Settings hub, grouped the way the design blueprint asks for. */
class SettingsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.settings_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        // ---------------- GENERAL ----------------
        b.section(getString(R.string.sec_general))
        val g = b.card()
        b.navRow(g, getString(R.string.s_appearance), null) { go(AppearanceActivity::class.java) }; b.divider(g)
        b.navRow(g, getString(R.string.s_homepage), null) { go(HomeShortcutsActivity::class.java) }; b.divider(g)
        b.navRow(g, getString(R.string.s_search_engine), null) { go(SearchEngineActivity::class.java) }; b.divider(g)
        b.navRow(g, getString(R.string.s_toolbar_nav), null) { go(ToolbarActivity::class.java) }

        // ---------------- BROWSING ----------------
        b.section(getString(R.string.sec_browsing))
        val br = b.card()
        b.navRow(br, getString(R.string.s_tabs), null) { go(TabsActivity::class.java) }; b.divider(br)
        b.navRow(br, getString(R.string.s_bookmarks), null) { go(BookmarksActivity::class.java) }; b.divider(br)
        b.navRow(br, getString(R.string.s_history), null) { go(HistoryActivity::class.java) }; b.divider(br)
        b.navRow(br, getString(R.string.s_downloads), null) {
            try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) } catch (_: Exception) { }
        }; b.divider(br)
        b.navRow(br, getString(R.string.s_website_settings), null) { go(AdvancedActivity::class.java) }

        // ---------------- PRIVACY & SECURITY ----------------
        b.section(getString(R.string.sec_privacy))
        val p = b.card()
        b.navRow(p, getString(R.string.r_privacy_dashboard), null) { go(PrivacyActivity::class.java) }; b.divider(p)
        b.navRow(p, getString(R.string.s_tracking), null) { go(ShieldsActivity::class.java) }; b.divider(p)
        b.navRow(p, getString(R.string.s_cookies), null) { go(ShieldsActivity::class.java) }; b.divider(p)
        b.navRow(p, getString(R.string.s_permissions), null) { go(AdvancedActivity::class.java) }; b.divider(p)
        b.navRow(p, getString(R.string.s_passwords), null) { go(PasswordsActivity::class.java) }; b.divider(p)
        b.navRow(p, getString(R.string.s_clear_data), null) { go(PrivacyActivity::class.java) }

        // ---------------- PERFORMANCE ----------------
        b.section(getString(R.string.sec_performance))
        val pf = b.card()
        b.navRow(pf, getString(R.string.s_data_usage), null) { go(ShieldsActivity::class.java) }; b.divider(pf)
        b.navRow(pf, getString(R.string.s_cache), null) { go(PrivacyActivity::class.java) }; b.divider(pf)
        b.navRow(pf, getString(R.string.s_background), null) { go(AdvancedActivity::class.java) }

        // ---------------- ACCESSIBILITY ----------------
        b.section(getString(R.string.sec_accessibility))
        val ac = b.card()
        b.navRow(ac, getString(R.string.s_text_size), null) { go(AppearanceActivity::class.java) }; b.divider(ac)
        b.navRow(ac, getString(R.string.s_reader_translate), null) {
            showDialog(getString(R.string.s_reader_translate), getString(R.string.reader_note))
        }; b.divider(ac)
        b.navRow(ac, getString(R.string.s_accessibility), null) { go(AppearanceActivity::class.java) }

        // ---------------- ADVANCED ----------------
        b.section(getString(R.string.sec_advanced))
        val ad = b.card()
        b.navRow(ad, getString(R.string.s_backup), null) { go(BackupActivity::class.java) }; b.divider(ad)
        b.navRow(ad, getString(R.string.s_developer), null) { go(AdvancedActivity::class.java) }; b.divider(ad)
        b.navRow(ad, getString(R.string.s_experimental), null) {
            showDialog(getString(R.string.s_experimental), getString(R.string.exp_note))
        }; b.divider(ad)
        b.navRow(ad, getString(R.string.r_extensions), null) { go(ExtensionsActivity::class.java) }

        // ---------------- ABOUT ----------------
        b.section(getString(R.string.sec_about))
        val ab = b.card()
        b.valueRow(ab, getString(R.string.s_app_version), appVersion(), null); b.divider(ab)
        b.navRow(ab, getString(R.string.s_webview_version), null) {
            showDialog(getString(R.string.s_webview_version), webViewVersion())
        }; b.divider(ab)
        b.navRow(ab, getString(R.string.s_licences), null) {
            showDialog(getString(R.string.s_licences), getString(R.string.licences_note))
        }; b.divider(ab)
        b.navRow(ab, getString(R.string.s_privacy_policy), null) {
            showDialog(getString(R.string.s_privacy_policy), getString(R.string.policy_note))
        }; b.divider(ab)
        b.navRow(ab, getString(R.string.s_feedback), null) { feedback() }; b.divider(ab)
        b.navRow(ab, getString(R.string.s_updates), null) {
            showDialog(getString(R.string.s_updates), getString(R.string.updates_note))
        }; b.divider(ab)
        b.navRow(ab, getString(R.string.r_about), null) { go(AboutActivity::class.java) }
    }

    private fun go(cls: Class<*>) { startActivity(Intent(this, cls)) }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton(android.R.string.ok, null).show()
    }

    private fun appVersion(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
    } catch (_: Exception) { "?" }

    private fun webViewVersion(): String = try {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val pm = android.webkit.WebView.getCurrentWebViewPackage()
            (pm?.versionName ?: "unknown") + "  (" + (pm?.packageName ?: "") + ")"
        } else "unknown"
    } catch (_: Exception) { "unknown" }

    private fun feedback() {
        val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.feedback_email)))
        try { startActivity(i) }
        catch (_: Exception) { showDialog(getString(R.string.s_feedback), getString(R.string.feedback_email)) }
    }
}
