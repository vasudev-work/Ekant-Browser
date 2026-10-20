package com.vasudev.ekant.gecko

import android.content.Context
import java.net.URI

/** Host-list blocker wired into NavigationDelegate.onLoadRequest (top-level). */
class AdBlocker(context: Context) {

    private val hosts = HashSet<String>()

    init {
        try {
            context.assets.open("blocklist.txt").bufferedReader().forEachLine { raw ->
                val line = raw.trim()
                if (line.isNotEmpty() && !line.startsWith("#")) hosts.add(line.lowercase())
            }
        } catch (_: Exception) {
        }
    }

    fun isBlocked(url: String?): Boolean {
        val u = url ?: return false
        val host = try { URI(u).host?.lowercase() } catch (_: Exception) { null } ?: return false
        if (hosts.contains(host)) return true
        val parts = host.split(".")
        for (i in 1 until parts.size - 1) {
            if (hosts.contains(parts.subList(i, parts.size).joinToString("."))) return true
        }
        return false
    }
}
