package com.vasudev.ekant.browser

import android.net.Uri
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Global permission blocking plus a per-site allow list. */
class SitePermissionsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_permissions

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.sp_global))
        val g = b.card()
        b.switchRow(g, getString(R.string.adv_perms), getString(R.string.adv_perms_sum),
            prefs.getBoolean("block_perms", true)) { _, on ->
            prefs.edit().putBoolean("block_perms", on).apply()
        }
        b.note(getString(R.string.sp_note))

        b.section(getString(R.string.sp_sites))
        val hosts = knownHosts()
        if (hosts.isEmpty()) {
            b.note(getString(R.string.d_empty))
        } else {
            for (h in hosts) {
                val c = b.card()
                b.switchRow(c, h, null, allowlist().contains(h)) { _, on -> setAllow(h, on) }
            }
        }

        b.section(getString(R.string.ext_add))
        val a = b.card()
        b.navRow(a, getString(R.string.sp_add), getString(R.string.sp_add_sum)) { addDialog() }
    }

    private fun allowlist(): MutableList<String> =
        (prefs.getString("site_allow", "") ?: "").split("\n").filter { it.isNotBlank() }.toMutableList()

    private fun setAllow(host: String, on: Boolean) {
        val l = allowlist()
        if (on) { if (!l.contains(host)) l.add(host) } else l.remove(host)
        prefs.edit().putString("site_allow", l.joinToString("\n")).apply()
        rebuild()
    }

    private fun knownHosts(): List<String> {
        val set = LinkedHashSet<String>()
        for (e in (prefs.getString("history", "") ?: "").split("\n")) {
            val i = e.indexOf('\u0001')
            if (i < 0) continue
            try {
                val h = Uri.parse(e.substring(i + 1)).host
                if (!h.isNullOrBlank()) set.add(h)
            } catch (_: Exception) { }
            if (set.size >= 40) break
        }
        set.addAll(allowlist())
        return set.toList()
    }

    private fun addDialog() {
        val e = EditText(this)
        e.hint = getString(R.string.sp_host)
        e.setPadding(48, 24, 48, 24)
        AlertDialog.Builder(this).setTitle(R.string.sp_add).setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val h = e.text.toString().trim().lowercase()
                if (h.isNotEmpty()) setAllow(h, true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
