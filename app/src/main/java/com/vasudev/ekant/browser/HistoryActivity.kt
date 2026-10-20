package com.vasudev.ekant.browser

import android.widget.LinearLayout

class HistoryActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_history

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val entries = (prefs.getString("history", "") ?: "").split("\n").filter { it.contains('\u0001') }
        b.section(getString(R.string.s_history))
        if (entries.isEmpty()) {
            b.note(getString(R.string.d_empty))
        } else {
            for (e in entries.take(60)) {
                val i = e.indexOf('\u0001')
                val title = e.substring(0, i)
                val url = e.substring(i + 1)
                val card = b.card()
                b.valueRow(card, if (title.isBlank()) url else title, url.take(40), null)
            }
        }
        b.section(getString(R.string.priv_data))
        val c = b.card()
        b.navRow(c, getString(R.string.priv_clear_all), null) {
            prefs.edit().remove("history").apply(); rebuild()
        }
    }
}
