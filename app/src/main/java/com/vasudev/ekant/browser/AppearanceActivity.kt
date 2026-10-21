package com.vasudev.ekant.browser

import android.widget.LinearLayout

class AppearanceActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_appearance

    private fun setBrand(b: String) {
        prefs.edit().putString("theme_brand", b).apply()
        recreate()
    }

    private fun setRotation(m: String) {
        prefs.edit().putString("orientation", m).apply()
        recreate()
    }

    private fun setThemeMode(m: String) {
        prefs.edit().putString("theme_mode", m).apply()
        recreate()
    }

    private val zooms = intArrayOf(85, 100, 115, 130)
    private val zoomNames = intArrayOf(
        R.string.ap_small, R.string.ap_normal, R.string.ap_large, R.string.ap_huge
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.ap_brand))
        val cb = b.card()
        val brand = prefs.getString("theme_brand", "aekant")
        b.radioRow(cb, getString(R.string.brand_aekant), getString(R.string.brand_aekant_sum), brand == "aekant") { setBrand("aekant") }
        b.divider(cb)
        b.radioRow(cb, getString(R.string.brand_quiet), getString(R.string.brand_quiet_sum), brand == "quiet") { setBrand("quiet") }

        b.section(getString(R.string.ap_theme))
        val c0 = b.card()
        val mode = prefs.getString("theme_mode", "dark")
        b.radioRow(c0, getString(R.string.theme_dark), null, mode == "dark") { setThemeMode("dark") }
        b.divider(c0)
        b.radioRow(c0, getString(R.string.theme_light), null, mode == "light") { setThemeMode("light") }
        b.divider(c0)
        b.radioRow(c0, getString(R.string.theme_system), null, mode == "system") { setThemeMode("system") }

        b.section(getString(R.string.ap_sites))
        val c3 = b.card()
        b.switchRow(c3, getString(R.string.ap_force_dark), getString(R.string.ap_force_dark_sum),
            prefs.getBoolean("force_dark", false)) { _, on ->
            prefs.edit().putBoolean("force_dark", on).apply()
            recreate()
        }
        b.divider(c3)
        b.navRow(c3, getString(R.string.us_title), null) {
            startActivity(android.content.Intent(this, UserStylesActivity::class.java))
        }

        b.section(getString(R.string.ap_browsing))
        val c4 = b.card()
        val rot = prefs.getString("orientation", "system")
        b.radioRow(c4, getString(R.string.ap_rot_system), null, rot == "system") { setRotation("system") }
        b.divider(c4)
        b.radioRow(c4, getString(R.string.ap_rot_portrait), null, rot == "portrait") { setRotation("portrait") }
        b.divider(c4)
        b.radioRow(c4, getString(R.string.ap_rot_landscape), null, rot == "landscape") { setRotation("landscape") }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_desktop), getString(R.string.ap_desktop_sum),
            prefs.getBoolean("desktop_default", false)) { _, on ->
            prefs.edit().putBoolean("desktop_default", on).apply()
        }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_pull), getString(R.string.ap_pull_sum),
            prefs.getBoolean("pull_refresh", true)) { _, on ->
            prefs.edit().putBoolean("pull_refresh", on).apply()
        }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_external), getString(R.string.ap_external_sum),
            prefs.getBoolean("external_links", false)) { _, on ->
            prefs.edit().putBoolean("external_links", on).apply()
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
