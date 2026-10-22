package com.vasudev.ekant.browser

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Homepage: the built-in start page, or a site of your own. */
class HomepageActivity : BaseSettingsActivity() {

    override val titleRes = R.string.hp_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.hp_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.hp_on), getString(R.string.hp_on_sum),
            prefs.getBoolean("homepage_on", true)) { _, on ->
            prefs.edit().putBoolean("homepage_on", on).apply()
        }
        b.divider(c)
        val type = prefs.getString("homepage_type", "start") ?: "start"
        b.radioRow(c, getString(R.string.hp_start), getString(R.string.hp_start_sum), type == "start") {
            prefs.edit().putString("homepage_type", "start").apply(); rebuild()
        }
        b.divider(c)
        b.radioRow(c, getString(R.string.hp_custom), null, type == "custom") {
            prefs.edit().putString("homepage_type", "custom").apply(); rebuild()
        }
        b.divider(c)
        val url = prefs.getString("homepage_url", "") ?: ""
        b.navRow(c, getString(R.string.hp_url), url.ifBlank { getString(R.string.hp_url_empty) },
            android.view.View.OnClickListener { edit() })
        b.note(getString(R.string.hp_note))
    }

    private fun edit() {
        val e = EditText(this)
        e.hint = "https://example.com"
        e.setText(prefs.getString("homepage_url", "") ?: "")
        AlertDialog.Builder(this)
            .setTitle(R.string.hp_url)
            .setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                prefs.edit().putString("homepage_url", e.text.toString().trim())
                    .putString("homepage_type", "custom").apply()
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
