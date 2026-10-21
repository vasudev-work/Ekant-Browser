package com.vasudev.ekant.browser

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Regex URL blocking, for kiosk or parental use. */
class UrlFiltersActivity : BaseSettingsActivity() {

    override val titleRes = R.string.uf_title

    private fun patterns(): MutableList<String> =
        (prefs.getString("url_filters", "") ?: "").split("\n").filter { it.isNotBlank() }.toMutableList()

    private fun save(l: List<String>) {
        prefs.edit().putString("url_filters", l.joinToString("\n")).apply()
        rebuild()
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.uf_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.uf_enable), getString(R.string.uf_enable_sum),
            prefs.getBoolean("url_filter_on", false)) { _, on ->
            prefs.edit().putBoolean("url_filter_on", on).apply()
        }
        b.note(getString(R.string.uf_note))

        b.section(getString(R.string.uf_rules))
        val list = patterns()
        if (list.isEmpty()) {
            b.note(getString(R.string.uf_empty))
        } else {
            for (p in list) {
                val card = b.card()
                b.valueRow(card, p.take(46), "", null)
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) { save(patterns().filter { it != p }) }
            }
        }
        b.section(getString(R.string.ext_add))
        val a = b.card()
        b.navRow(a, getString(R.string.uf_add), getString(R.string.uf_add_sum)) { addDialog() }
    }

    private fun addDialog() {
        val e = EditText(this)
        e.hint = getString(R.string.uf_hint)
        AlertDialog.Builder(this)
            .setTitle(R.string.uf_add)
            .setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val p = e.text.toString().trim()
                if (p.isNotEmpty()) {
                    try { Regex(p); save(patterns() + p) } catch (_: Exception) { }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
