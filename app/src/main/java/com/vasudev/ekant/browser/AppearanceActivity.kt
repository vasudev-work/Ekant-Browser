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
        b.themeCards(c0, mode ?: "dark") { setThemeMode(it) }

        b.section(getString(R.string.ap_sites))
        val c3 = b.card()
        // the reference offers three, not two: follow the system, force light, force dark
        val look = prefs.getString("site_appearance",
            if (prefs.getBoolean("force_dark", false)) "dark" else "auto") ?: "auto"
        b.radioRow(c3, getString(R.string.ap_look_auto), getString(R.string.ap_look_auto_sum), look == "auto") {
            setLook("auto")
        }
        b.divider(c3)
        b.radioRow(c3, getString(R.string.ap_look_light), null, look == "light") { setLook("light") }
        b.divider(c3)
        b.radioRow(c3, getString(R.string.ap_look_dark), getString(R.string.ap_force_dark_sum), look == "dark") {
            setLook("dark")
        }
        b.divider(c3)
        b.navRow(c3, getString(R.string.us_title), null) {
            startActivity(android.content.Intent(this, UserStylesActivity::class.java))
        }

        b.section(getString(R.string.ap_zoom))
        val cz = b.card()
        val z = prefs.getInt("zoom", 100)
        for ((i, level) in listOf(80, 100, 120, 150).withIndex()) {
            if (i > 0) b.divider(cz)
            b.radioRow(cz, "$level%", null, z == level) {
                prefs.edit().putInt("zoom", level).apply()
                toast(getString(R.string.ap_zoom_set, level))
                recreate()
            }
        }
        val cz2 = b.card()
        b.switchRow(cz2, getString(R.string.ap_zoom_text), getString(R.string.ap_zoom_text_sum),
            prefs.getBoolean("zoom_text_only", true)) { _, on ->
            prefs.edit().putBoolean("zoom_text_only", on).apply()
            recreate()
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
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_scrollbars), getString(R.string.ap_scrollbars_sum),
            prefs.getBoolean("scrollbars", false)) { _, on ->
            prefs.edit().putBoolean("scrollbars", on).apply()
        }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_immersive), getString(R.string.ap_immersive_sum),
            prefs.getBoolean("immersive", false)) { _, on ->
            prefs.edit().putBoolean("immersive", on).apply()
        }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_force_zoom), getString(R.string.ap_force_zoom_sum),
            prefs.getBoolean("force_zoom", false)) { _, on ->
            prefs.edit().putBoolean("force_zoom", on).apply()
        }
        b.divider(c4)
        b.switchRow(c4, getString(R.string.ap_volume), getString(R.string.ap_volume_sum),
            prefs.getBoolean("volume_scroll", false)) { _, on ->
            prefs.edit().putBoolean("volume_scroll", on).apply()
        }

        if (android.os.Build.VERSION.SDK_INT >= 31) {
            b.divider(c0)
            b.switchRow(c0, getString(R.string.ap_dynamic), getString(R.string.ap_dynamic_sum),
                prefs.getBoolean("dynamic_color", false)) { _, on ->
                prefs.edit().putBoolean("dynamic_color", on).apply()
            }
        }
        b.divider(c0)
        b.switchRow(c0, getString(R.string.ap_amoled_ui), getString(R.string.ap_amoled_ui_sum),
            prefs.getBoolean("amoled_ui", false)) { _, on ->
            prefs.edit().putBoolean("amoled_ui", on).apply()
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

    /** One pref, and the old boolean kept in step so nothing else has to change. */
    private fun setLook(v: String) {
        prefs.edit()
            .putString("site_appearance", v)
            .putBoolean("force_dark", v == "dark")
            .apply()
        recreate()
    }
}
