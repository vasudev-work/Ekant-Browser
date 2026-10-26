package com.aekantkit

import android.content.Context

/**
 * "Block an element" - the areas a user has picked on a page, kept per host.
 *
 * Each entry is one CSS selector. They are appended to the page's stylesheet on
 * every load, so a blocked banner stays blocked without touching the filter lists.
 */
object ElementBlocks {

    private const val KEY = "elem_blocks"
    private const val SEP = "\u0001"

    private fun prefs(c: Context) = c.getSharedPreferences("aekantkit", Context.MODE_PRIVATE)

    private fun lines(c: Context): List<String> =
        (prefs(c).getString(KEY, "") ?: "").split("\n").filter { it.contains(SEP) }

    /** host to selector pairs, newest first. */
    fun all(c: Context): List<Pair<String, String>> =
        lines(c).map { it.substringBefore(SEP) to it.substringAfter(SEP) }

    fun countFor(c: Context, host: String): Int = all(c).count { it.first == host }

    fun add(c: Context, host: String, selector: String) {
        if (host.isBlank() || selector.isBlank()) return
        val l = lines(c).toMutableList()
        val line = host.lowercase() + SEP + selector
        l.remove(line)
        l.add(0, line)
        while (l.size > 500) l.removeAt(l.size - 1)
        prefs(c).edit().putString(KEY, l.joinToString("\n")).apply()
    }

    fun clear(c: Context, host: String? = null) {
        if (host == null) { prefs(c).edit().remove(KEY).apply(); return }
        val keep = lines(c).filter { !it.startsWith(host.lowercase() + SEP) }
        prefs(c).edit().putString(KEY, keep.joinToString("\n")).apply()
    }

    /** The stylesheet fragment for one host. */
    fun cssFor(c: Context, host: String): String {
        val h = host.lowercase()
        val rules = all(c).filter { it.first == h }.map { it.second }
        if (rules.isEmpty()) return ""
        return rules.joinToString("") { "$it{display:none!important}" }
    }
}
