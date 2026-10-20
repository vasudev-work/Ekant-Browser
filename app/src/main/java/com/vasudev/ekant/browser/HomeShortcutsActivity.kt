package com.vasudev.ekant.browser

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Edit the new-tab shortcuts and the recently-visited widget. */
class HomeShortcutsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_home_shortcuts

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val raw = prefs.getString("shortcuts", "") ?: ""
        b.section(getString(R.string.hs_current))
        if (raw.isBlank()) {
            b.note(getString(R.string.hs_defaults))
        } else {
            val items = raw.split(";;").filter { it.isNotBlank() }
            for (i in items.indices) {
                val name = items[i].substringBefore("|")
                val url = items[i].substringAfter("|", "")
                val card = b.card()
                b.valueRow(card, name, url.take(34), null)
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) { removeAt(i) }
            }
        }

        b.section(getString(R.string.ext_add))
        val add = b.card()
        b.navRow(add, getString(R.string.hs_add), getString(R.string.hs_add_sum)) { addDialog() }
        b.divider(add)
        b.navRow(add, getString(R.string.hs_reset), null) {
            prefs.edit().remove("shortcuts").apply(); rebuild()
        }

        b.section(getString(R.string.hs_start_style))
        val st = b.card()
        val style = prefs.getString("start_style", "personal")
        b.radioRow(st, getString(R.string.hs_style_minimal), getString(R.string.hs_style_minimal_sum), style == "minimal") { setStyle("minimal") }
        b.divider(st)
        b.radioRow(st, getString(R.string.hs_style_personal), getString(R.string.hs_style_personal_sum), style == "personal") { setStyle("personal") }
        b.divider(st)
        b.radioRow(st, getString(R.string.hs_style_dashboard), getString(R.string.hs_style_dashboard_sum), style == "dashboard") { setStyle("dashboard") }

        b.section(getString(R.string.hs_recent))
        val rc = b.card()
        b.switchRow(rc, getString(R.string.hs_show_recent), getString(R.string.hs_show_recent_sum),
            prefs.getBoolean("show_recent", true)) { _, on ->
            prefs.edit().putBoolean("show_recent", on).apply()
        }
    }

    private fun setStyle(s: String) {
        prefs.edit().putString("start_style", s).apply()
        rebuild()
    }

    private fun list(): MutableList<String> =
        (prefs.getString("shortcuts", "") ?: "").split(";;").filter { it.isNotBlank() }.toMutableList()

    private fun save(items: List<String>) {
        prefs.edit().putString("shortcuts", items.joinToString(";;")).apply()
        rebuild()
    }

    private fun removeAt(i: Int) {
        val l = list(); if (i in l.indices) { l.removeAt(i); save(l) }
    }

    private fun addDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.hs_name) }
        val url = EditText(this).apply { hint = getString(R.string.hs_url) }
        box.addView(name); box.addView(url)
        AlertDialog.Builder(this).setTitle(R.string.hs_add).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val n = name.text.toString().trim()
                var u = url.text.toString().trim()
                if (n.isEmpty() || u.isEmpty()) return@setPositiveButton
                if (!u.startsWith("http")) u = "https://$u"
                val l = list(); l.add(n + "|" + u); save(l)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
