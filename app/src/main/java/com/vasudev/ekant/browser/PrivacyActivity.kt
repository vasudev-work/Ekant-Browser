package com.vasudev.ekant.browser

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast

/** One screen showing the current protection state + data controls. */
class PrivacyActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_privacy_dashboard

    private fun onOff(v: Boolean) = if (v) "On" else "Off"

    private fun confirmReset() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.reset_identity)
            .setMessage(R.string.reset_identity_q)
            .setPositiveButton(R.string.reset_identity) { _, _ -> resetNow() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun resetNow() {
        try {
            val cm = android.webkit.CookieManager.getInstance()
            cm.removeAllCookies(null); cm.flush()
        } catch (_: Exception) { }
        try { android.webkit.WebStorage.getInstance().deleteAllData() } catch (_: Exception) { }
        try { android.webkit.WebView(this).clearCache(true) } catch (_: Exception) { }
        android.widget.Toast.makeText(this, getString(R.string.reset_done), android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.priv_protection))
        val c1 = b.card()
        b.valueRow(c1, getString(R.string.sh_ads), onOff(prefs.getBoolean("block_ads", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.sh_trackers), onOff(prefs.getBoolean("block_trackers", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.sh_3p), onOff(prefs.getBoolean("block_3p", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.adv_https), onOff(prefs.getBoolean("https_only", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.adv_fp), onOff(prefs.getBoolean("fingerprint", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.adv_perms), onOff(prefs.getBoolean("block_perms", true)), null); b.divider(c1)
        b.valueRow(c1, getString(R.string.adv_memory), onOff(prefs.getBoolean("memory_saver", true)), null)

        b.section(getString(R.string.reset_identity))
        val ri = b.card()
        b.navRow(ri, getString(R.string.reset_identity), getString(R.string.reset_identity_sum),
            android.view.View.OnClickListener { confirmReset() })

        b.section(getString(R.string.priv_data))
        val c2 = b.card()
        b.navRow(c2, getString(R.string.priv_clear_all), getString(R.string.r_clear_sum)) { clearAll() }; b.divider(c2)
        b.navRow(c2, getString(R.string.priv_clear_cookies), null) {
            CookieManager.getInstance().removeAllCookies(null); toast(getString(R.string.t_cleared))
        }; b.divider(c2)
        b.navRow(c2, getString(R.string.priv_clear_cache), null) {
            WebView(this).clearCache(true); toast(getString(R.string.t_cleared))
        }

        b.note(getString(R.string.priv_note))
    }

    private fun clearAll() {
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        WebView(this).clearCache(true)
        prefs.edit().remove("history").remove("bookmarks").apply()
        toast(getString(R.string.t_cleared))
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
