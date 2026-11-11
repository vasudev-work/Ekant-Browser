package com.vasudev.ekant.browser

import android.widget.LinearLayout

/**
 * Sites that are blocked on purpose, and when.
 *
 * A blocklist with no clock is a wall you climb over once. This one can be set to
 * apply only during the hours you choose, so "no YouTube at work" means what it
 * says and the evening stays yours.
 */
class FocusActivity : BaseSettingsActivity() {

    override val titleRes = R.string.focus_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val always = prefs.getBoolean("block_always", false)
        val schedOn = prefs.getBoolean("block_sched_on", false)

        b.section(getString(R.string.focus_when))
        val c0 = b.card()
        b.switchRow(c0, getString(R.string.focus_always), getString(R.string.focus_always_sum), always) { _, on ->
            prefs.edit().putBoolean("block_always", on).apply()
            recreate()
        }
        b.divider(c0)
        b.switchRow(c0, getString(R.string.focus_sched), getString(R.string.focus_sched_sum), schedOn) { _, on ->
            prefs.edit().putBoolean("block_sched_on", on).apply()
            recreate()
        }
        if (!always && schedOn) {
            b.divider(c0)
            val from = prefs.getInt("block_from", 9)
            val to = prefs.getInt("block_to", 18)
            b.navRow(c0, getString(R.string.focus_from), hourText(from)) { pickHour("block_from", from) }
            b.divider(c0)
            b.navRow(c0, getString(R.string.focus_to), hourText(to)) { pickHour("block_to", to) }
        }
        if (!always && !schedOn) b.note(getString(R.string.focus_off_note))

        b.section(getString(R.string.focus_sites))
        val list = b.card()
        b.iconRow(list, R.drawable.ic_add, getString(R.string.focus_add), getString(R.string.focus_add_sum)) {
            addDialog()
        }
        val sites = blocked()
        if (sites.isEmpty()) {
            b.note(getString(R.string.focus_empty))
        } else {
            val c2 = b.card()
            sites.forEachIndexed { i, host ->
                if (i > 0) b.divider(c2)
                b.navRow(c2, host, getString(R.string.focus_remove)) {
                    prefs.edit().putString("block_sites",
                        blocked().filterNot { it == host }.joinToString("\n")).apply()
                    toast(getString(R.string.focus_removed, host))
                    recreate()
                }
            }
        }

        b.note(getString(R.string.focus_note))
    }

    private fun blocked(): List<String> =
        (prefs.getString("block_sites", "") ?: "").split("\n").map { it.trim() }.filter { it.isNotBlank() }

    private fun hourText(h: Int) = String.format("%02d:00", h)

    private fun pickHour(key: String, current: Int) {
        val hours = (0..23).map { String.format("%02d:00", it) }.toTypedArray()
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.focus_pick_hour))
            .setSingleChoiceItems(hours, current) { d, which ->
                prefs.edit().putInt(key, which).apply()
                d.dismiss()
                recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addDialog() {
        val e = android.widget.EditText(this)
        e.hint = getString(R.string.focus_hint)
        e.setSingleLine(true)
        e.inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        val pad = dp(22)
        val wrap = LinearLayout(this)
        wrap.setPadding(pad, dp(10), pad, dp(4))
        wrap.addView(e)

        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.focus_add))
            .setView(wrap)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                var host = e.text.toString().trim().lowercase()
                host = host.removePrefix("https://").removePrefix("http://").substringBefore('/')
                if (host.isBlank()) return@setPositiveButton
                val now = blocked().toMutableList()
                if (!now.contains(host)) now.add(host)
                prefs.edit().putString("block_sites", now.joinToString("\n")).apply()
                toast(getString(R.string.focus_added, host))
                recreate()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
