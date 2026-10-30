package com.vasudev.ekant.browser

import android.net.Uri
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/**
 * Permissions, per site.
 *
 * The global switch stays as the default for sites nobody has decided about.
 * Every site you have visited then gets its own matrix: camera, microphone and
 * location each set to Allow, Ask or Block. Ask means the page's request comes
 * to you as a dialog, which is what a browser should do by default.
 */
class SitePermissionsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_permissions

    private val kinds = listOf(
        "camera" to R.string.sp_camera,
        "microphone" to R.string.sp_mic,
        "location" to R.string.sp_location
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.sp_global))
        val g = b.card()
        b.switchRow(g, getString(R.string.adv_perms), getString(R.string.adv_perms_sum),
            prefs.getBoolean("block_perms", true)) { _, on ->
            prefs.edit().putBoolean("block_perms", on).apply()
            rebuild()
        }
        b.note(getString(R.string.sp_note))

        b.section(getString(R.string.sp_sites))
        val hosts = knownHosts()
        if (hosts.isEmpty()) {
            b.note(getString(R.string.d_empty))
        } else {
            for (h in hosts) {
                val c = b.card()
                b.navRow(c, h, summary(h)) { matrixDialog(h) }
            }
        }

        b.section(getString(R.string.ext_add))
        val a = b.card()
        b.navRow(a, getString(R.string.sp_add), getString(R.string.sp_add_sum)) { addDialog() }
    }

    private fun state(host: String, kind: String): String {
        val v = prefs.getString("perm_" + host + "_" + kind, "") ?: ""
        if (v.isNotBlank()) return v
        return if (prefs.getBoolean("block_perms", true)) "block" else "ask"
    }

    private fun setState(host: String, kind: String, value: String) {
        prefs.edit().putString("perm_" + host + "_" + kind, value).apply()
    }

    private fun label(v: String): String = getString(
        when (v) {
            "allow" -> R.string.sp_allow
            "block" -> R.string.sp_block
            else -> R.string.sp_ask
        })

    private fun summary(host: String): String {
        val parts = kinds.map { label(state(host, it.first)) }
        return parts.joinToString(" · ")
    }

    /** Tap a row to cycle it: Allow -> Ask -> Block -> Allow. */
    private fun matrixDialog(host: String) {
        val labels = kinds.map { getString(it.second) + "  —  " + label(state(host, it.first)) }
        AlertDialog.Builder(this)
            .setTitle(host)
            .setItems(labels.toTypedArray()) { _, which ->
                val kind = kinds[which].first
                val next = when (state(host, kind)) {
                    "allow" -> "ask"
                    "ask" -> "block"
                    else -> "allow"
                }
                setState(host, kind, next)
                rebuild()
                matrixDialog(host)
            }
            .setNeutralButton(getString(R.string.sp_reset)) { _, _ ->
                for (k in kinds) prefs.edit().remove("perm_" + host + "_" + k.first).apply()
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun knownHosts(): List<String> {
        val set = LinkedHashSet<String>()
        for (e in (prefs.getString("history", "") ?: "").split("\n")) {
            val u = e.split('\u0001').getOrElse(1) { "" }
            if (u.isBlank()) continue
            try {
                val h = Uri.parse(u).host
                if (!h.isNullOrBlank()) set.add(h)
            } catch (_: Exception) { }
            if (set.size >= 40) break
        }
        for (k in prefs.all.keys) {
            if (!k.startsWith("perm_")) continue
            val rest = k.removePrefix("perm_")
            for (kind in kinds) {
                val suffix = "_" + kind.first
                if (rest.endsWith(suffix)) set.add(rest.removeSuffix(suffix))
            }
        }
        return set.toList().sorted()
    }

    private fun addDialog() {
        val e = EditText(this)
        e.hint = getString(R.string.sp_host)
        e.setPadding(48, 24, 48, 24)
        AlertDialog.Builder(this).setTitle(R.string.sp_add).setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val h = e.text.toString().trim().lowercase()
                if (h.isNotEmpty()) { setState(h, "camera", "ask"); rebuild(); matrixDialog(h) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
