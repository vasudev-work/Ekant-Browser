package com.vasudev.ekant.browser

import java.util.Collections

/** Collects media URLs seen while browsing (network requests and page markup). */
object MediaSniffer {

    private val items = Collections.synchronizedMap(LinkedHashMap<String, String>())

    @Volatile var host: String = ""

    fun reset(h: String) {
        if (h == host) return
        host = h
        items.clear()
    }

    fun add(url: String, type: String) {
        if (url.startsWith("blob:") || url.startsWith("data:")) return
        if (!looksMedia(url)) return
        if (items.size > 300) items.clear()
        items[url] = if (type.isBlank()) kind(url) else type
    }

    fun all(): List<Pair<String, String>> =
        synchronized(items) { items.entries.map { it.key to it.value }.reversed() }

    fun looksMedia(u: String): Boolean {
        val p = u.substringBefore('?').substringBefore('#').lowercase()
        return p.endsWith(".mp4") || p.endsWith(".webm") || p.endsWith(".m4a") || p.endsWith(".mp3") ||
            p.endsWith(".ogg") || p.endsWith(".m3u8") || p.endsWith(".mpd") || p.endsWith(".mov") ||
            p.endsWith(".mkv") || p.endsWith(".aac")
    }

    fun kind(u: String): String {
        val p = u.substringBefore('?').lowercase()
        return when {
            p.endsWith(".m3u8") -> "HLS"
            p.endsWith(".mpd") -> "DASH"
            p.endsWith(".mp4") || p.endsWith(".webm") || p.endsWith(".mov") || p.endsWith(".mkv") -> "Video"
            else -> "Audio"
        }
    }
}
