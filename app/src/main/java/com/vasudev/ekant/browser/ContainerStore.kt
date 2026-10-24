package com.vasudev.ekant.browser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Local-only persistence for containers and their tabs (no cloud sync). */
class ContainerStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aekant_containers", Context.MODE_PRIVATE)

    val palette = listOf(
        "#FF6600", "#8CC63F", "#3B82F6", "#A855F7", "#EF4444", "#14B8A6"
    )

    fun getContainers(): List<Container> {
        val raw = prefs.getString(KEY_CONTAINERS, null)
        val list = ArrayList<Container>()
        if (!raw.isNullOrEmpty()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(Container(o.getString("id"), o.getString("name"), o.getString("color"),
                        o.optString("ua", "")))
                }
            } catch (_: Exception) {
                list.clear()
            }
        }
        if (list.isEmpty()) {
            val def = defaultContainers()
            saveContainers(def)
            return def
        }
        return list
    }

    fun saveContainers(containers: List<Container>) {
        val arr = JSONArray()
        for (c in containers) {
            arr.put(JSONObject().put("id", c.id).put("name", c.name).put("color", c.color).put("ua", c.ua))
        }
        prefs.edit().putString(KEY_CONTAINERS, arr.toString()).apply()
    }

    fun newContainer(name: String, color: String): Container {
        val c = Container(UUID.randomUUID().toString(), name, color)
        val list = getContainers().toMutableList()
        list.add(c)
        saveContainers(list)
        return c
    }

    fun removeContainer(id: String) {
        saveContainers(getContainers().filter { it.id != id })
        prefs.edit().remove(tabsKey(id)).apply()
        if (prefs.getString(KEY_ACTIVE, null) == id) prefs.edit().remove(KEY_ACTIVE).apply()
    }

    var activeContainerId: String?
        get() = prefs.getString(KEY_ACTIVE, null)
        set(value) = prefs.edit().putString(KEY_ACTIVE, value).apply()

    fun getTabs(containerId: String): List<TabState> {
        val raw = prefs.getString(tabsKey(containerId), null) ?: return emptyList()
        val list = ArrayList<TabState>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(TabState(o.getString("url"), o.optString("title", "")))
            }
        } catch (_: Exception) {
            list.clear()
        }
        return list
    }

    fun saveTabs(containerId: String, tabs: List<TabState>) {
        val arr = JSONArray()
        for (t in tabs) arr.put(JSONObject().put("url", t.url).put("title", t.title))
        prefs.edit().putString(tabsKey(containerId), arr.toString()).apply()
    }

    private fun defaultContainers(): List<Container> = listOf(
        Container(UUID.randomUUID().toString(), "Personal", palette[0]),
        Container(UUID.randomUUID().toString(), "Work", palette[1]),
        Container(UUID.randomUUID().toString(), "Banking", palette[2]),
        Container(UUID.randomUUID().toString(), "Social", palette[3])
    )

    private fun tabsKey(id: String) = "tabs_$id"

    companion object {
        private const val KEY_CONTAINERS = "containers"
        private const val KEY_ACTIVE = "active_container"
    }
}
