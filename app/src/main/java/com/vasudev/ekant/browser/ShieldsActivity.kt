package com.vasudev.ekant.browser

import android.widget.LinearLayout

class ShieldsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_shields

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val c = b.card()

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
    }
}
