package com.vasudev.ekant.browser

import android.widget.LinearLayout

/** Reading and read-aloud behaviour, each with its own on/off switch. */
class ReadingActivity : BaseSettingsActivity() {

    override val titleRes = R.string.rd_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.rd_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.rd_bionic), getString(R.string.rd_bionic_sum),
            prefs.getBoolean("bionic", false)) { _, on ->
            prefs.edit().putBoolean("bionic", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_focus), getString(R.string.rd_focus_sum),
            prefs.getBoolean("tts_focus", true)) { _, on ->
            prefs.edit().putBoolean("tts_focus", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_battery), getString(R.string.rd_battery_sum),
            prefs.getBoolean("tts_battery", true)) { _, on ->
            prefs.edit().putBoolean("tts_battery", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_retry), getString(R.string.rd_retry_sum),
            prefs.getBoolean("auto_retry", true)) { _, on ->
            prefs.edit().putBoolean("auto_retry", on).apply()
        }
        b.note(getString(R.string.rd_note))
    }
}
