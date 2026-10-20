package com.vasudev.ekant.browser

import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Saved articles: mark read/unread, open, remove. */
class ReadingListActivity : BaseSettingsActivity() {

    override val titleRes = R.string.rl_title

    private fun items(): MutableList<String> =
        (prefs.getString("reading", "") ?: "").split("\n").filter { it.contains('\u0001') }.toMutableList()

    private fun save(l: List<String>) {
        prefs.edit().putString("reading", l.joinToString("\n")).apply()
        rebuild()
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val list = items()
        b.section(getString(R.string.rl_title))
        if (list.isEmpty()) {
            b.note(getString(R.string.rl_empty))
        } else {
            for (e in list) {
                val p = e.split('\u0001')
                val title = p.getOrElse(0) { "" }
                val url = p.getOrElse(1) { "" }
                val read = p.getOrElse(2) { "0" } == "1"
                val card = b.card()
                b.switchRow(card, if (read) "\u2713 " + title else title, url.take(38), read) { _, on ->
                    toggleRead(e, on)
                }
                b.divider(card)
                b.navRow(card, getString(R.string.rl_open), null) {
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) { }
                }
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) {
                    save(items().filter { it != e })
                }
            }
        }
        b.section(getString(R.string.priv_data))
        val c = b.card()
        b.navRow(c, getString(R.string.priv_clear_all), null) {
            AlertDialog.Builder(this).setMessage(R.string.rl_clear_confirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.edit().remove("reading").apply(); rebuild()
                }
                .setNegativeButton(android.R.string.cancel, null).show()
        }
    }

    private fun toggleRead(target: String, read: Boolean) {
        val l = items().map { e ->
            if (e == target) {
                val p = e.split('\u0001')
                (p.getOrElse(0) { "" }) + "\u0001" + (p.getOrElse(1) { "" }) + "\u0001" + (if (read) "1" else "0")
            } else e
        }
        save(l)
    }
}
