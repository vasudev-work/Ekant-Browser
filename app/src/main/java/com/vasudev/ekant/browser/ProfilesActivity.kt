package com.vasudev.ekant.browser

import android.net.Uri
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Default profile plus a profile for individual sites. */
class ProfilesActivity : BaseSettingsActivity() {

    override val titleRes = R.string.pf_title

    private fun label(p: String): String = when (p) {
        Profiles.TRUSTED -> getString(R.string.pf_trusted)
        Profiles.RESTRICTED -> getString(R.string.pf_restricted)
        else -> getString(R.string.pf_standard)
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.pf_default))
        val d = b.card()
        val cur = Profiles.defaultProfile(this)
        b.radioRow(d, getString(R.string.pf_trusted), getString(R.string.pf_trusted_sum), cur == Profiles.TRUSTED) {
            Profiles.setDefault(this, Profiles.TRUSTED); rebuild()
        }
        b.divider(d)
        b.radioRow(d, getString(R.string.pf_standard), getString(R.string.pf_standard_sum), cur == Profiles.STANDARD) {
            Profiles.setDefault(this, Profiles.STANDARD); rebuild()
        }
        b.divider(d)
        b.radioRow(d, getString(R.string.pf_restricted), getString(R.string.pf_restricted_sum), cur == Profiles.RESTRICTED) {
            Profiles.setDefault(this, Profiles.RESTRICTED); rebuild()
        }
        b.note(getString(R.string.pf_note))

        b.section(getString(R.string.pf_sites))
        val hosts = knownHosts()
        if (hosts.isEmpty()) {
            b.note(getString(R.string.d_empty))
        } else {
            for (h in hosts) {
                val card = b.card()
                b.valueRow(card, h, label(Profiles.profileFor(this, h)), null)
                b.divider(card)
                b.navRow(card, getString(R.string.pf_change), null) { pick(h) }
            }
        }
    }

    private fun pick(host: String) {
        val names = arrayOf(
            getString(R.string.pf_trusted), getString(R.string.pf_standard), getString(R.string.pf_restricted))
        AlertDialog.Builder(this)
            .setTitle(host)
            .setItems(names) { _, which ->
                when (which) {
                    0 -> Profiles.assign(this, host, Profiles.TRUSTED)
                    1 -> Profiles.remove(this, host)
                    else -> Profiles.assign(this, host, Profiles.RESTRICTED)
                }
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
        for (a in Profiles.assignments(this)) set.add(a.first)
        return set.toList()
    }
}
