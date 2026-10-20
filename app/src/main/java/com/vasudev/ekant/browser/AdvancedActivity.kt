package com.vasudev.ekant.browser

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Power-user switches: memory saver, HTTPS-only, fingerprint noise, permissions,
 *  swipe gestures, remote debugging and a custom User-Agent. */
class AdvancedActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_power

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val c1 = b.card()
        b.switchRow(c1, getString(R.string.adv_memory), getString(R.string.adv_memory_sum),
            prefs.getBoolean("memory_saver", true)) { _, on ->
            prefs.edit().putBoolean("memory_saver", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.adv_https), getString(R.string.adv_https_sum),
            prefs.getBoolean("https_only", true)) { _, on ->
            prefs.edit().putBoolean("https_only", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.adv_fp), getString(R.string.adv_fp_sum),
            prefs.getBoolean("fingerprint", true)) { _, on ->
            prefs.edit().putBoolean("fingerprint", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.adv_perms), getString(R.string.adv_perms_sum),
            prefs.getBoolean("block_perms", true)) { _, on ->
            prefs.edit().putBoolean("block_perms", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.adv_swipe), getString(R.string.adv_swipe_sum),
            prefs.getBoolean("swipe", true)) { _, on ->
            prefs.edit().putBoolean("swipe", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.adv_debug), getString(R.string.adv_debug_sum),
            prefs.getBoolean("remote_debug", false)) { _, on ->
            prefs.edit().putBoolean("remote_debug", on).apply()
        }

        b.section(getString(R.string.adv_ua))
        val c2 = b.card()
        val ua = prefs.getString("ua_custom", "") ?: ""
        b.valueRow(c2, getString(R.string.adv_ua), if (ua.isBlank()) "Default" else ua.take(38), null)
        b.divider(c2)
        b.navRow(c2, getString(R.string.adv_ua_set), null) { uaDialog() }
        b.divider(c2)
        b.navRow(c2, getString(R.string.adv_ua_clear), null) {
            prefs.edit().remove("ua_custom").apply()
            rebuild()
        }

        b.note(getString(R.string.adv_note))
    }

    private fun uaDialog() {
        val e = EditText(this)
        e.hint = getString(R.string.adv_ua_hint)
        e.setText(prefs.getString("ua_custom", ""))
        AlertDialog.Builder(this).setTitle(R.string.adv_ua_set).setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                prefs.edit().putString("ua_custom", e.text.toString().trim()).apply()
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
