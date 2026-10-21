package com.vasudev.ekant.browser

import android.content.Context

/** Per-domain privacy profiles: Trusted, Standard or Restricted. */
object Profiles {

    const val TRUSTED = "trusted"
    const val STANDARD = "standard"
    const val RESTRICTED = "restricted"

    private fun prefs(c: Context) =
        c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    fun defaultProfile(c: Context): String =
        prefs(c).getString("profile_default", STANDARD) ?: STANDARD

    fun setDefault(c: Context, p: String) {
        prefs(c).edit().putString("profile_default", p).apply()
    }

    fun profileFor(c: Context, host: String): String {
        val h = host.lowercase()
        if (h.isBlank()) return defaultProfile(c)
        for (line in (prefs(c).getString("domain_profiles", "") ?: "").split("\n")) {
            val i = line.indexOf('\u0001')
            if (i <= 0) continue
            if (line.substring(0, i).equals(h, true)) return line.substring(i + 1)
        }
        return defaultProfile(c)
    }

    fun assign(c: Context, host: String, profile: String) {
        val h = host.trim().lowercase()
        if (h.isEmpty()) return
        val keep = assignments(c).filterNot { it.first == h }
        val next = keep + (h to profile)
        prefs(c).edit().putString("domain_profiles",
            next.joinToString("\n") { it.first + '\u0001' + it.second }).apply()
    }

    fun remove(c: Context, host: String) {
        val next = assignments(c).filterNot { it.first == host }
        prefs(c).edit().putString("domain_profiles",
            next.joinToString("\n") { it.first + '\u0001' + it.second }).apply()
    }

    fun assignments(c: Context): List<Pair<String, String>> {
        val out = ArrayList<Pair<String, String>>()
        for (line in (prefs(c).getString("domain_profiles", "") ?: "").split("\n")) {
            val i = line.indexOf('\u0001')
            if (i <= 0) continue
            out.add(line.substring(0, i) to line.substring(i + 1))
        }
        return out
    }
}
