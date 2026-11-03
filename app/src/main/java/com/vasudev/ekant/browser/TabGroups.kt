package com.vasudev.ekant.browser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Tab groups.
 *
 * A group is a name and the pages in it. It keeps URLs rather than live tabs on
 * purpose: a WebView dies with the process, a URL does not, so a group survives
 * a restart and can be reopened later.
 */
object TabGroups {

    data class Group(val id: String, val name: String, val urls: List<String>, val colour: Int)

    private const val KEY = "tab_groups"
    private val COLOURS = intArrayOf(0xFF1A73E8.toInt(), 0xFF0C9054.toInt(), 0xFFE2543A.toInt(),
                                     0xFF7B4BFF.toInt(), 0xFF00A8A2.toInt(), 0xFFF5A623.toInt())

    private fun sp(c: Context): SharedPreferences =
        c.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    fun all(c: Context): List<Group> = try {
        val raw = sp(c).getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val urls = o.optJSONArray("urls") ?: JSONArray()
            Group(o.optString("id"), o.optString("name"),
                  (0 until urls.length()).map { urls.getString(it) },
                  o.optInt("colour", COLOURS[0]))
        }
    } catch (_: Exception) { emptyList() }

    private fun save(c: Context, list: List<Group>) {
        val arr = JSONArray()
        list.forEach { g ->
            val o = JSONObject()
            o.put("id", g.id)
            o.put("name", g.name)
            o.put("colour", g.colour)
            val u = JSONArray(); g.urls.forEach { u.put(it) }
            o.put("urls", u)
            arr.put(o)
        }
        sp(c).edit().putString(KEY, arr.toString()).apply()
    }

    /** A new group from whatever is open now. */
    fun create(c: Context, name: String, urls: List<String>): Group {
        val list = all(c)
        val g = Group("g" + System.currentTimeMillis(), name, urls,
                      COLOURS[list.size % COLOURS.size])
        save(c, list + g)
        return g
    }

    fun rename(c: Context, id: String, name: String) =
        save(c, all(c).map { if (it.id == id) it.copy(name = name) else it })

    fun remove(c: Context, id: String) = save(c, all(c).filterNot { it.id == id })
}
