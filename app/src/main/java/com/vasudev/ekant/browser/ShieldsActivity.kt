package com.vasudev.ekant.browser

import android.widget.LinearLayout

class ShieldsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_shields

    override fun build(b: SettingsBuilder, content: LinearLayout) {
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
        b.switchRow(c, getString(R.string.sh_popups), getString(R.string.sh_popups_sum),
            prefs.getBoolean("popups", true)) { _, on ->
            prefs.edit().putBoolean("popups", on).apply()
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
}
