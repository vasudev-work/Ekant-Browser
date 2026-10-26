package com.aekantkit

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.util.HashSet

/**
 * Built-in ad and tracker blocker (carried over from the Aekant build).
 * Host lists ship in assets/ads.txt and assets/trackers.txt.
 */
class AdBlocker(context: Context) {

    private val adHosts = HashSet<String>()
    private val trackerHosts = HashSet<String>()

    @Volatile var blockAds: Boolean = true
    @Volatile var blockTrackers: Boolean = true

    init {
        load(context, "ads.txt", adHosts)
        load(context, "trackers.txt", trackerHosts)
    }

    private fun load(context: Context, asset: String, into: HashSet<String>) {
        try {
            context.assets.open(asset).use { input ->
                BufferedReader(InputStreamReader(input)).forEachLine { raw ->
                    val line = raw.trim()
                    if (line.isEmpty() || line.startsWith("#")) return@forEachLine
                    into.add(line.lowercase())
                }
            }
        } catch (_: Exception) {
        }
    }

    fun isBlocked(url: String): Boolean {
        if (!blockAds && !blockTrackers) return false
        val host = hostOf(url) ?: return false
        if (blockAds && matches(adHosts, host)) return true
        if (blockTrackers && matches(trackerHosts, host)) return true
        return false
    }

    private fun matches(set: HashSet<String>, host: String): Boolean {
        if (set.contains(host)) return true
        val parts = host.split(".")
        for (i in 1 until parts.size - 1) {
            if (set.contains(parts.subList(i, parts.size).joinToString("."))) return true
        }
        return false
    }

    private fun hostOf(url: String): String? = try {
        URI(url).host?.lowercase()
    } catch (_: Exception) {
        null
    }
}
