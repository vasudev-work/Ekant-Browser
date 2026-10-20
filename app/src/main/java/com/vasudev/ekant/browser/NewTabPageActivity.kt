package com.vasudev.ekant.browser

import android.widget.LinearLayout

class NewTabPageActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_ntp

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.ntp_bg_sec))
        val c1 = b.card()
        b.switchRow(c1, getString(R.string.ntp_bg), null, prefs.getBoolean("ntp_bg", true)) { _, on ->
            prefs.edit().putBoolean("ntp_bg", on).apply()
        }

        b.section(getString(R.string.ntp_widgets))
        val c2 = b.card()
        b.switchRow(c2, getString(R.string.ntp_topsites), null, prefs.getBoolean("ntp_topsites", true)) { _, on ->
            prefs.edit().putBoolean("ntp_topsites", on).apply()
        }
        b.divider(c2)
        b.switchRow(c2, getString(R.string.ntp_stats), null, prefs.getBoolean("ntp_stats", true)) { _, on ->
            prefs.edit().putBoolean("ntp_stats", on).apply()
        }

        b.section(getString(R.string.ntp_opening))
        val c3 = b.card()
        val op = prefs.getString("ntp_open", "last")
        b.radioRow(c3, getString(R.string.ntp_last), null, op == "last") { pick("last") }
        b.divider(c3)
        b.radioRow(c3, getString(R.string.ntp_new_1h), null, op == "1h") { pick("1h") }
        b.divider(c3)
        b.radioRow(c3, getString(R.string.ntp_new), null, op == "new") { pick("new") }
    }

    private fun pick(val0: String) {
        prefs.edit().putString("ntp_open", val0).apply()
        recreate()
    }
}
