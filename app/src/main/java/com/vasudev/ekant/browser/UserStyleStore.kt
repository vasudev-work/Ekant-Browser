package com.vasudev.ekant.browser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Stylus-like user styles: CSS applied to matching sites. */
object UserStyleStore {

    private const val KEY = "user_css"

    private fun prefs(c: Context) =
        c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    fun all(c: Context): MutableList<Triple<String, String, Boolean>> {
        val out = ArrayList<Triple<String, String, Boolean>>()
        val raw = prefs(c).getString(KEY, "") ?: ""
        if (raw.isBlank()) return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(Triple(o.optString("p", "*"), o.optString("c", ""), o.optBoolean("e", true)))
            }
        } catch (_: Exception) { }
        return out
    }

    fun save(c: Context, list: List<Triple<String, String, Boolean>>) {
        val arr = JSONArray()
        for (t in list) arr.put(JSONObject().put("p", t.first).put("c", t.second).put("e", t.third))
        prefs(c).edit().putString(KEY, arr.toString()).apply()
    }

    fun add(c: Context, pattern: String, css: String) {
        if (css.isBlank()) return
        val l = all(c)
        l.add(Triple(pattern.ifBlank { "*" }, css, true))
        save(c, l)
    }

    fun delete(c: Context, index: Int) {
        val l = all(c)
        if (index in l.indices) { l.removeAt(index); save(c, l) }
    }

    fun setEnabled(c: Context, index: Int, on: Boolean) {
        val l = all(c)
        if (index in l.indices) {
            val t = l[index]
            l[index] = Triple(t.first, t.second, on)
            save(c, l)
        }
    }

    fun cssFor(c: Context, url: String): String {
        val sb = StringBuilder()
        for (t in all(c)) {
            if (!t.third || t.second.isBlank()) continue
            if (matches(t.first, url)) sb.append(t.second).append('\n')
        }
        return sb.toString()
    }

    private fun matches(pattern: String, url: String): Boolean {
        val p = pattern.trim()
        if (p.isEmpty() || p == "*") return true
        val rx = "^" + p.split("*").joinToString(".*") { Regex.escape(it) } + "$"
        return try { Regex(rx, RegexOption.IGNORE_CASE).containsMatchIn(url) } catch (_: Exception) { false }
    }
}
