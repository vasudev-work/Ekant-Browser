package com.vasudev.ekant.browser

import android.widget.LinearLayout

class AutofillActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_autofill

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val useBrowser = prefs.getBoolean("af_browser", true)

        val c1 = b.card()
        b.radioRow(c1, getString(R.string.af_browser), getString(R.string.af_browser_sum), useBrowser) {
            pick(true)
        }
        b.divider(c1)
        b.radioRow(c1, getString(R.string.af_other), getString(R.string.af_other_sum), !useBrowser) {
            pick(false)
        }

        b.note(getString(R.string.af_note))

        val c2 = b.card()
        b.switchRow(c2, getString(R.string.af_private), getString(R.string.af_private_sum),
            prefs.getBoolean("af_private", true)) { _, on ->
            prefs.edit().putBoolean("af_private", on).apply()
        }
    }

    private fun pick(browser: Boolean) {
        prefs.edit().putBoolean("af_browser", browser).apply()
        recreate()
    }
}
