package com.vasudev.ekant.browser

import android.widget.LinearLayout

class AppearanceActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_appearance

    private val zooms = intArrayOf(85, 100, 115, 130)
    private val zoomNames = intArrayOf(
        R.string.ap_small, R.string.ap_normal, R.string.ap_large, R.string.ap_huge
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val c1 = b.card()
        b.switchRow(c1, getString(R.string.ap_dark), getString(R.string.ap_dark_sum),
            prefs.getBoolean("dark", true)) { _, on ->
            prefs.edit().putBoolean("dark", on).apply()
            recreate()
        }

        b.section(getString(R.string.ap_text))
        val c2 = b.card()
        val cur = prefs.getInt("zoom", 100)
        for (i in zooms.indices) {
            if (i > 0) b.divider(c2)
            val z = zooms[i]
            b.radioRow(c2, getString(zoomNames[i]), null, cur == z) {
                prefs.edit().putInt("zoom", z).apply()
                recreate()
            }
        }
    }
}
