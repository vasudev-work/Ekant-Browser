package com.vasudev.ekant.browser

import android.widget.LinearLayout

class ShieldsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_shields

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.sh_level))
        val lv = b.card()
        val level = prefs.getString("shield_level", "std") ?: "std"
        b.radioRow(lv, getString(R.string.sh_level_std), getString(R.string.sh_level_std_sum), level == "std") {
            setLevel("std")
        }
        b.divider(lv)
        b.radioRow(lv, getString(R.string.sh_level_strict), getString(R.string.sh_level_strict_sum), level == "strict") {
            setLevel("strict")
        }
        b.divider(lv)
        b.radioRow(lv, getString(R.string.sh_level_custom), getString(R.string.sh_level_custom_sum), level == "custom") {
            prefs.edit().putString("shield_level", "custom").apply(); recreate()
        }

        val c = b.card()

        b.switchRow(c, getString(R.string.sh_easylist), getString(R.string.sh_easylist_sum),
            prefs.getBoolean("easylist", true)) { _, on ->
            prefs.edit().putBoolean("easylist", on).apply()
            AdFilterEngine.enabled = on
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_ads), getString(R.string.sh_ads_sum),
            prefs.getBoolean("block_ads", true)) { _, on ->
            prefs.edit().putBoolean("block_ads", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_trackers), getString(R.string.sh_trackers_sum),
            prefs.getBoolean("block_trackers", true)) { _, on ->
            prefs.edit().putBoolean("block_trackers", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_clear_exit), getString(R.string.sh_clear_exit_sum),
            prefs.getBoolean("clear_on_exit", false)) { _, on ->
            prefs.edit().putBoolean("clear_on_exit", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_secure_screen), getString(R.string.sh_secure_screen_sum),
            prefs.getBoolean("secure_screen", false)) { _, on ->
            prefs.edit().putBoolean("secure_screen", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_clear_close), getString(R.string.sh_clear_close_sum),
            prefs.getBoolean("clear_on_close", false)) { _, on ->
            prefs.edit().putBoolean("clear_on_close", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_fonts), getString(R.string.sh_fonts_sum),
            prefs.getBoolean("block_fonts", false)) { _, on ->
            prefs.edit().putBoolean("block_fonts", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_popups), getString(R.string.sh_popups_sum),
            prefs.getBoolean("popups", true)) { _, on ->
            prefs.edit().putBoolean("popups", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_webrtc), getString(R.string.sh_webrtc_sum),
            prefs.getBoolean("webrtc_block", false)) { _, on ->
            prefs.edit().putBoolean("webrtc_block", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_app_launch), getString(R.string.sh_app_launch_sum),
            prefs.getBoolean("block_app_launch", false)) { _, on ->
            prefs.edit().putBoolean("block_app_launch", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_safe_browsing), getString(R.string.sh_safe_browsing_sum),
            prefs.getBoolean("safe_browsing", true)) { _, on ->
            prefs.edit().putBoolean("safe_browsing", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_clean_urls), getString(R.string.sh_clean_urls_sum),
            prefs.getBoolean("clean_urls", true)) { _, on ->
            prefs.edit().putBoolean("clean_urls", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_3p), getString(R.string.sh_3p_sum),
            prefs.getBoolean("block_3p", true)) { _, on ->
            prefs.edit().putBoolean("block_3p", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_scripts), getString(R.string.sh_scripts_sum),
            !prefs.getBoolean("js", true)) { _, checked ->
            prefs.edit().putBoolean("js", !checked).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_cookies), getString(R.string.sh_cookies_sum),
            !prefs.getBoolean("cookies", true)) { _, checked ->
            prefs.edit().putBoolean("cookies", !checked).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_images), getString(R.string.sh_images_sum),
            !prefs.getBoolean("images", true)) { _, checked ->
            prefs.edit().putBoolean("images", !checked).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sh_bg_media), getString(R.string.sh_bg_media_sum),
            prefs.getBoolean("bg_media", false)) { _, on ->
            prefs.edit().putBoolean("bg_media", on).apply()
        }
    }

    /** A level is a preset for switches that already exist, not a new engine. */
    private fun setLevel(level: String) {
        prefs.edit()
            .putString("shield_level", level)
            .putBoolean("block_ads", true)
            .putBoolean("sh_easylist", true)
            .putBoolean("sh_trackers", true)
            .putBoolean("sh_3p", level == "strict")
            .putBoolean("sh_fonts", level == "strict")
            .putBoolean("sh_popups", level == "strict")
            .apply()
        android.widget.Toast.makeText(this,
            getString(if (level == "strict") R.string.sh_level_strict else R.string.sh_level_std),
            android.widget.Toast.LENGTH_SHORT).show()
        recreate()
    }
}
