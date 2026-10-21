package com.vasudev.ekant.browser

import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Ad-blocking summary, switches and the per-site allow list. */
class AdBlockActivity : BaseSettingsActivity() {

    override val titleRes = R.string.ab_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        // summary
        val stats = b.card()
        b.valueRow(stats, getString(R.string.ab_blocked), "" + AdFilterEngine.blockedCount, null)
        b.divider(stats)
        b.valueRow(stats, getString(R.string.ab_saved), "" + (AdFilterEngine.savedBytes / 1024) + " KB", null)

        b.section(getString(R.string.ab_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.ab_enable), getString(R.string.ab_enable_sum),
            prefs.getBoolean("easylist", true)) { _, on ->
            prefs.edit().putBoolean("easylist", on).apply()
            AdFilterEngine.enabled = on
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.ab_ads), getString(R.string.ab_ads_sum),
            prefs.getBoolean("block_ads", true)) { _, on ->
            prefs.edit().putBoolean("block_ads", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.ab_trackers), getString(R.string.ab_trackers_sum),
            prefs.getBoolean("block_trackers", true)) { _, on ->
            prefs.edit().putBoolean("block_trackers", on).apply()
        }
        b.note(getString(R.string.ab_note, AdFilterEngine.ruleCount()))

        b.section(getString(R.string.ab_allowlist))
        val hosts = AdFilterEngine.allowList()
        if (hosts.isEmpty()) {
            b.note(getString(R.string.ab_allow_empty))
        } else {
            for (h in hosts) {
                val card = b.card()
                b.switchRow(card, h, null, true) { _, on ->
                    AdFilterEngine.allow(h, on); rebuild()
                }
            }
        }
        b.section(getString(R.string.ext_add))
        val a = b.card()
        b.navRow(a, getString(R.string.ab_add_site), getString(R.string.ab_add_site_sum)) { addDialog() }
        b.divider(a)
        b.navRow(a, getString(R.string.ab_reset), null) {
            AdFilterEngine.resetStats(); rebuild()
        }
    }

    private fun addDialog() {
        val e = EditText(this)
        e.hint = getString(R.string.sp_host)
        e.inputType = InputType.TYPE_TEXT_VARIATION_URI
        AlertDialog.Builder(this)
            .setTitle(R.string.ab_add_site)
            .setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                AdFilterEngine.allow(e.text.toString(), true); rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
