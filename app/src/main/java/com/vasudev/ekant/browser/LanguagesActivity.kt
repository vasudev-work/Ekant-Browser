package com.vasudev.ekant.browser

import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class LanguagesActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_languages

    // All 22 scheduled Indian languages + English (India).
    private val indianLanguages = listOf(
        "English (India)", "\u0985\u09b8\u09ae\u09c0\u09af\u09bc\u09be", "\u09ac\u09be\u0982\u09b2\u09be", "\u092c\u0930'", "\u0921\u094b\u0917\u0930\u0940",
        "\u0a97\u0ac1\u0a9c\u0ab0\u0abe\u0aa4\u0ac0", "\u0939\u093f\u0928\u094d\u0926\u0940", "\u0c95\u0ca8\u0ccd\u0ca8\u0ca1", "\u0915\u0949\u0936\u0941\u0930 / \u0643\u0623\u0634\u064f\u0631", "\u0915\u094b\u0902\u0915\u0923\u0940",
        "\u092e\u0948\u0925\u093f\u0932\u0940", "\u0d2e\u0d32\u0d2f\u0d3e\u0d33\u0d02", "\u09ae\u09c8\u09a4\u09c8\u09b2\u09cb\u09a8\u09cd", "\u092e\u0930\u093e\u0920\u0940", "\u0928\u0947\u092a\u093e\u0932\u0940",
        "\u0b13\u0b21\u0b3c\u0b3f\u0b06", "\u0a2a\u0a70\u0a1c\u0a3e\u0a2c\u0a40", "\u0938\u0902\u0938\u094d\u0915\u0943\u0924\u092e\u094d", "\u0938\u0902\u0925\u093e\u0932\u0940", "\u0938\u093f\u0902\u0927\u0940 / \u0633\u0646\u068c\u064a",
        "\u0ba4\u0bae\u0bbf\u0bb4\u0bcd", "\u0c24\u0c46\u0c32\u0c41\u0c17\u0c41", "\u0627\u0631\u062f\u0648"
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.lang_section))
        val c1 = b.card()
        b.valueRow(c1, getString(R.string.lang_device), getString(R.string.lang_en_in), null)

        val c2 = b.card()
        b.switchRow(c2, getString(R.string.lang_translate), null, prefs.getBoolean("lang_translate", true)) { _, on ->
            prefs.edit().putBoolean("lang_translate", on).apply()
        }

        b.section(getString(R.string.lang_preferred))
        b.note(getString(R.string.lang_preferred_desc))
        val c3 = b.card()
        for ((i, name) in indianLanguages.withIndex()) {
            if (i > 0) b.divider(c3)
            b.navRow(c3, name, null, null)
        }
        b.divider(c3)
        b.navRow(c3, getString(R.string.lang_add), null) {
            AlertDialog.Builder(this).setTitle(R.string.lang_add)
                .setMessage(R.string.info_chromium)
                .setPositiveButton(R.string.d_close, null).show()
        }
    }
}
