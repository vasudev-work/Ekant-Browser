package com.vasudev.ekant.browser

import android.widget.LinearLayout

/** Reading and read-aloud behaviour, each with its own on/off switch. */
class ReadingActivity : BaseSettingsActivity() {

    override val titleRes = R.string.rd_title

    private fun dictDialog() {
        val col = android.widget.LinearLayout(this)
        col.orientation = android.widget.LinearLayout.VERTICAL
        col.setPadding(48, 24, 48, 8)
        val w = android.widget.EditText(this)
        w.hint = getString(R.string.rd_word)
        col.addView(w)
        val say = android.widget.EditText(this)
        say.hint = getString(R.string.rd_say)
        col.addView(say)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.rd_dict_add)
            .setView(col)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val from = w.text.toString().trim()
                val to = say.text.toString().trim()
                if (from.isNotEmpty() && to.isNotEmpty()) {
                    val cur = (prefs.getString("tts_dict", "") ?: "").split("\n").filter { it.isNotBlank() }
                    prefs.edit().putString("tts_dict", (cur + (from + '\u0001' + to)).joinToString("\n")).apply()
                    rebuild()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.rd_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.rd_bionic), getString(R.string.rd_bionic_sum),
            prefs.getBoolean("bionic", false)) { _, on ->
            prefs.edit().putBoolean("bionic", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_focus), getString(R.string.rd_focus_sum),
            prefs.getBoolean("tts_focus", true)) { _, on ->
            prefs.edit().putBoolean("tts_focus", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_battery), getString(R.string.rd_battery_sum),
            prefs.getBoolean("tts_battery", true)) { _, on ->
            prefs.edit().putBoolean("tts_battery", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_retry), getString(R.string.rd_retry_sum),
            prefs.getBoolean("auto_retry", true)) { _, on ->
            prefs.edit().putBoolean("auto_retry", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.rd_keep_screen), getString(R.string.rd_keep_screen_sum),
            prefs.getBoolean("keep_screen_on", false)) { _, on ->
            prefs.edit().putBoolean("keep_screen_on", on).apply()
        }
        b.section(getString(R.string.rd_visual))
        val v = b.card()
        b.switchRow(v, getString(R.string.rd_eink), getString(R.string.rd_eink_sum),
            prefs.getBoolean("eink", false)) { _, on ->
            prefs.edit().putBoolean("eink", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_autoscroll), getString(R.string.rd_autoscroll_sum),
            prefs.getBoolean("autoscroll", false)) { _, on ->
            prefs.edit().putBoolean("autoscroll", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_paginate), getString(R.string.rd_paginate_sum),
            prefs.getBoolean("paginate", false)) { _, on ->
            prefs.edit().putBoolean("paginate", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_live), getString(R.string.rd_live_sum),
            prefs.getBoolean("tts_live", true)) { _, on ->
            prefs.edit().putBoolean("tts_live", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_highlight), getString(R.string.rd_highlight_sum),
            prefs.getBoolean("highlight", true)) { _, on ->
            prefs.edit().putBoolean("highlight", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_amoled), getString(R.string.rd_amoled_sum),
            prefs.getBoolean("amoled", false)) { _, on ->
            prefs.edit().putBoolean("amoled", on).apply()
        }
        b.divider(v)
        b.switchRow(v, getString(R.string.rd_keepalive), getString(R.string.rd_keepalive_sum),
            prefs.getBoolean("keep_alive", true)) { _, on ->
            prefs.edit().putBoolean("keep_alive", on).apply()
        }
        b.divider(v)
        val spd = prefs.getInt("autoscroll_px", 2)
        val speeds = intArrayOf(1, 2, 4)
        val speedNames = intArrayOf(R.string.rd_slow, R.string.rd_normal, R.string.rd_fast)
        for (i in speeds.indices) {
            if (i > 0) b.divider(v)
            b.radioRow(v, getString(speedNames[i]), null, spd == speeds[i]) {
                prefs.edit().putInt("autoscroll_px", speeds[i]).apply(); rebuild()
            }
        }

        b.section(getString(R.string.rd_page_font))
        val f = b.card()
        val pfs = intArrayOf(0, 16, 18, 20, 22)
        val pfNames = intArrayOf(R.string.rd_off, R.string.rd_small, R.string.rd_normal, R.string.rd_large, R.string.rd_huge)
        val curPf = prefs.getInt("page_font", 0)
        for (i in pfs.indices) {
            if (i > 0) b.divider(f)
            b.radioRow(f, getString(pfNames[i]), null, curPf == pfs[i]) {
                prefs.edit().putInt("page_font", pfs[i]).apply(); rebuild()
            }
        }

        b.section(getString(R.string.rd_voice))
        val vo = b.card()
        val pit = prefs.getFloat("tts_pitch", 1.0f)
        val pitches = floatArrayOf(0.85f, 1.0f, 1.15f, 1.3f)
        val pitchNames = intArrayOf(R.string.rd_low, R.string.rd_default, R.string.rd_high, R.string.rd_veryhigh)
        for (i in pitches.indices) {
            if (i > 0) b.divider(vo)
            b.radioRow(vo, getString(pitchNames[i]), null, Math.abs(pit - pitches[i]) < 0.01f) {
                prefs.edit().putFloat("tts_pitch", pitches[i]).apply()
                TtsReader.setPitch(pitches[i])
                rebuild()
            }
        }

        b.section(getString(R.string.rd_dict))
        val dict = (prefs.getString("tts_dict", "") ?: "").split("\n").filter { it.contains('\u0001') }
        if (dict.isEmpty()) {
            b.note(getString(R.string.rd_dict_empty))
        } else {
            for (line in dict) {
                val i = line.indexOf('\u0001')
                val from = line.substring(0, i)
                val to = line.substring(i + 1)
                val card = b.card()
                b.valueRow(card, from, to, null)
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) {
                    val keep = dict.filter { it != line }
                    prefs.edit().putString("tts_dict", keep.joinToString("\n")).apply()
                    rebuild()
                }
            }
        }
        b.section(getString(R.string.ext_add))
        val add = b.card()
        b.navRow(add, getString(R.string.rd_dict_add), getString(R.string.rd_dict_add_sum)) { dictDialog() }

        b.note(getString(R.string.rd_note))
    }
}
