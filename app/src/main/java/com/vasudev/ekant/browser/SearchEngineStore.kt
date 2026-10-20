package com.vasudev.ekant.browser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SearchEngine(val name: String, val template: String)

/** User-editable search engines. Templates use %s for the query. */
class SearchEngineStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aekant_engines", Context.MODE_PRIVATE)

    fun getAll(): MutableList<SearchEngine> {
        val raw = prefs.getString("engines", null)
        val list = ArrayList<SearchEngine>()
        if (!raw.isNullOrEmpty()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(SearchEngine(o.getString("name"), o.getString("template")))
                }
            } catch (_: Exception) { list.clear() }
        }
        if (list.isEmpty()) {
            list.addAll(defaults())
            save(list)
        }
        return list
    }

    fun save(list: List<SearchEngine>) {
        val arr = JSONArray()
        for (e in list) arr.put(JSONObject().put("name", e.name).put("template", e.template))
        prefs.edit().putString("engines", arr.toString()).apply()
    }

    fun add(e: SearchEngine) { val l = getAll(); l.add(e); save(l) }

    fun update(oldName: String, e: SearchEngine) {
        val l = getAll()
        val i = l.indexOfFirst { it.name == oldName }
        if (i >= 0) { l[i] = e; save(l) }
    }

    fun delete(name: String) {
        save(getAll().filter { it.name != name })
        if (defaultName == name) defaultName = getAll().firstOrNull()?.name ?: "DuckDuckGo"
    }

    var defaultName: String
        get() = prefs.getString("default", "DuckDuckGo") ?: "DuckDuckGo"
        set(v) = prefs.edit().putString("default", v).apply()

    fun default(): SearchEngine {
        val all = getAll()
        return all.firstOrNull { it.name == defaultName } ?: all.first()
    }

    fun urlFor(query: String): String {
        val t = default().template
        val enc = android.net.Uri.encode(query)
        return if (t.contains("%s")) t.replace("%s", enc) else t + enc
    }

    private fun defaults(): List<SearchEngine> = listOf(
        SearchEngine("Google", "https://www.google.com/search?q=%s"),
        SearchEngine("Bing", "https://www.bing.com/search?q=%s"),
        SearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
        SearchEngine("Brave Search", "https://search.brave.com/search?q=%s"),
        SearchEngine("Ecosia", "https://www.ecosia.org/search?q=%s"),
        SearchEngine("Qmuma", "https://qmuma.com/search?q=%s"),
        SearchEngine("iBharat", "https://ibharat.org/search?q=%s"),
        SearchEngine("Indus AI", "https://indus.ai/?q=%s"),
        SearchEngine("Sarvam AI", "https://sarvam.ai/?q=%s")
    )
}
