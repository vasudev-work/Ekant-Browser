package com.vasudev.ekant.browser

import android.widget.LinearLayout

class BookmarksActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_bookmarks

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val entries = (prefs.getString("bookmarks", "") ?: "").split("\n").filter { it.contains('\u0001') }
        b.section(getString(R.string.s_bookmarks))
        if (entries.isEmpty()) {
            b.note(getString(R.string.d_empty))
        } else {
            for (e in entries) {
                val i = e.indexOf('\u0001')
                val title = e.substring(0, i)
                val url = e.substring(i + 1)
                val card = b.card()
                b.valueRow(card, if (title.isBlank()) url else title, url.take(40), null)
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) { removeEntry(e) }
            }
        }
        b.section(getString(R.string.priv_data))
        val c = b.card()
        b.navRow(c, getString(R.string.priv_clear_all), null) {
            prefs.edit().remove("bookmarks").apply(); rebuild()
        }
    }

    private fun removeEntry(target: String) {
        val l = (prefs.getString("bookmarks", "") ?: "").split("\n").filter { it.isNotBlank() && it != target }
        prefs.edit().putString("bookmarks", l.joinToString("\n")).apply()
        rebuild()
    }
}
