package com.vasudev.ekant.browser

import android.widget.LinearLayout

class TabsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_tabs

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val c = b.card()
        b.switchRow(c, getString(R.string.tabs_restore), getString(R.string.tabs_restore_sum),
            prefs.getBoolean("restore_tabs", true)) { _, on ->
            prefs.edit().putBoolean("restore_tabs", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.tabs_private_sep), getString(R.string.tabs_private_sep_sum),
            prefs.getBoolean("private_separate", true)) { _, on ->
            prefs.edit().putBoolean("private_separate", on).apply()
        }
        b.note(getString(R.string.tabs_note))
    }
}
