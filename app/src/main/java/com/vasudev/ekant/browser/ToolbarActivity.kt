package com.vasudev.ekant.browser

import android.widget.LinearLayout

/** Toolbar / navigation placement and compact mode. */
class ToolbarActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_toolbar_nav

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.tb_addr))
        val c1 = b.card()
        val addr = prefs.getString("address_bar", "top")
        b.radioRow(c1, getString(R.string.tb_top), null, addr == "top") { set("address_bar", "top") }
        b.divider(c1)
        b.radioRow(c1, getString(R.string.tb_bottom), null, addr == "bottom") { set("address_bar", "bottom") }

        b.section(getString(R.string.tb_nav))
        val c2 = b.card()
        val nav = prefs.getString("toolbar_pos", "bottom")
        b.radioRow(c2, getString(R.string.tb_bottom), null, nav == "bottom") { set("toolbar_pos", "bottom") }
        b.divider(c2)
        b.radioRow(c2, getString(R.string.tb_top), null, nav == "top") { set("toolbar_pos", "top") }

        b.section(getString(R.string.tb_behaviour))
        val c3 = b.card()
        b.switchRow(c3, getString(R.string.adv_swipe), getString(R.string.adv_swipe_sum),
            prefs.getBoolean("swipe", true)) { _, on -> prefs.edit().putBoolean("swipe", on).apply() }
        b.divider(c3)
        b.switchRow(c3, getString(R.string.tb_compact), getString(R.string.tb_compact_sum),
            prefs.getBoolean("compact", false)) { _, on -> prefs.edit().putBoolean("compact", on).apply() }
        b.divider(c3)
        b.switchRow(c3, getString(R.string.tb_focus), getString(R.string.tb_focus_sum),
            prefs.getBoolean("focus_mode", false)) { _, on -> prefs.edit().putBoolean("focus_mode", on).apply() }
    }

    private fun set(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        recreate()
    }
}
