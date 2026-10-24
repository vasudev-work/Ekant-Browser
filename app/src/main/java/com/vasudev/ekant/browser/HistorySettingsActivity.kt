package com.vasudev.ekant.browser

import android.view.View
import android.widget.LinearLayout

/** How long browsing history is kept. */
class HistorySettingsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.hs_title

    private val options = listOf(
        "none" to R.string.hs_none,
        "day" to R.string.hs_day,
        "week" to R.string.hs_week,
        "month" to R.string.hs_month,
        "6month" to R.string.hs_6month,
        "year" to R.string.hs_year,
        "unlimited" to R.string.hs_unlimited
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.hs_title))
        val cur = prefs.getString("history_keep", "unlimited") ?: "unlimited"
        val c = b.card()
        for ((i, o) in options.withIndex()) {
            if (i > 0) b.divider(c)
            b.radioRow(c, getString(o.second), null, cur == o.first) {
                prefs.edit().putString("history_keep", o.first).apply()
                MainActivity.pruneHistory(this)
                rebuild()
            }
        }

        b.section(getString(R.string.hs_now))
        val d = b.card()
        b.navRow(d, getString(R.string.s_clear_history), null,
            View.OnClickListener {
                prefs.edit().remove("history").apply()
                android.widget.Toast.makeText(this, getString(R.string.hs_cleared),
                    android.widget.Toast.LENGTH_SHORT).show()
                rebuild()
            })
        b.note(getString(R.string.hs_note))
    }
}
