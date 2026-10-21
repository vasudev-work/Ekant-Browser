package com.vasudev.ekant.browser

import android.widget.LinearLayout

/** Live list of recent requests and whether the blocker stopped them. */
class NetLogActivity : BaseSettingsActivity() {

    override val titleRes = R.string.nl_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.nl_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.nl_enable), getString(R.string.nl_enable_sum),
            prefs.getBoolean("net_log", false)) { _, on ->
            prefs.edit().putBoolean("net_log", on).apply()
            NetLog.enabled = on
        }
        b.divider(c)
        b.navRow(c, getString(R.string.nl_clear), null) { NetLog.clear(); rebuild() }

        val list = NetLog.entries()
        b.section(getString(R.string.nl_recent))
        if (list.isEmpty()) {
            b.note(getString(R.string.nl_empty))
        } else {
            val card = b.card()
            for (i in list.indices) {
                if (i > 0) b.divider(card)
                val line = list[i]
                val blocked = line.contains("BLOCK")
                b.valueRow(card, if (blocked) "blocked" else "allowed", line.take(70), null)
            }
        }
        b.note(getString(R.string.nl_note))
    }
}
