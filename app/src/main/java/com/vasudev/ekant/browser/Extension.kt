package com.vasudev.ekant.browser

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * A user extension: JavaScript that is injected into pages whose URL matches
 * [pattern]. This is the WebView-compatible form of an "extension" - it cannot
 * use the Chrome extension APIs (those need a Chromium engine), but it lets a
 * user add, upload, edit and run their own scripts.
 */
data class Extension(
    val id: String,
    val name: String,
    val pattern: String,
    val code: String,
    val enabled: Boolean,
    val builtIn: Boolean = false,
    val desc: String = "",
    val runAt: String = RUN_END,
    val grants: List<String> = emptyList()
) {
    /** pattern supports '*' wildcards and comma-separated alternatives. */
    fun matches(url: String): Boolean {
        val p = pattern.trim()
        if (p.isEmpty() || p == "*") return true
        return p.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .any { wildcardMatch(it, url) }
    }

    /** document-start scripts run before the page builds itself. */
    fun isStart(): Boolean = runAt == RUN_START

    /** True when the script expects the GM_* helper APIs. */
    fun wantsGm(): Boolean =
        grants.any { it != "none" } || code.contains("GM_")

    private fun wildcardMatch(pat: String, url: String): Boolean {
        val sb = StringBuilder("^")
        for (c in pat) {
            if (c == '*') sb.append(".*") else sb.append(Regex.escape(c.toString()))
        }
        sb.append("$")
        return try {
            Regex(sb.toString(), RegexOption.IGNORE_CASE).matches(url)
        } catch (_: Exception) {
            false
        }
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("name", name).put("pattern", pattern)
        .put("code", code).put("enabled", enabled).put("builtIn", builtIn)
        .put("desc", desc).put("runAt", runAt)
        .put("grants", JSONArray(grants))

    companion object {
        const val RUN_START = "document-start"
        const val RUN_END = "document-end"

        fun fromJson(o: JSONObject): Extension {
            val g = ArrayList<String>()
            o.optJSONArray("grants")?.let { for (i in 0 until it.length()) g.add(it.optString(i)) }
            return Extension(
                id = o.getString("id"),
                name = o.getString("name"),
                pattern = o.optString("pattern", "*"),
                code = o.optString("code", ""),
                enabled = o.optBoolean("enabled", true),
                builtIn = o.optBoolean("builtIn", false),
                desc = o.optString("desc", ""),
                runAt = o.optString("runAt", RUN_END),
                grants = g
            )
        }

        fun new(
            name: String, pattern: String, code: String, desc: String = "",
            runAt: String = RUN_END, grants: List<String> = emptyList()
        ) = Extension(UUID.randomUUID().toString(), name, pattern, code, true, false, desc, runAt, grants)
    }
}

/**
 * Reads the metadata block that Tampermonkey-style scripts carry, so a user can
 * paste a script and have Aekant work out the name, the description and where it
 * should run on its own.
 *
 *     // ==UserScript==
 *     // @name        Wide GitHub
 *     // @description Full width on github.com
 *     // @match       *github.com*
 *     // @run-at      document-end
 *     // @grant       GM_setValue
 *     // ==/UserScript==
 */
object UserScriptMeta {

    data class Meta(
        val name: String?,
        val desc: String?,
        val version: String?,
        val matches: List<String>,
        val includes: List<String>,
        val excludes: List<String>,
        val runAt: String,
        val grants: List<String>
    ) {
        /** matches and includes, joined the way the pattern field expects. */
        val pattern: String get() = (matches + includes).distinct().joinToString(",")
        val usesGm: Boolean get() = grants.any { it != "none" }
        val found: Boolean get() = name != null || pattern.isNotBlank() || grants.isNotEmpty()
    }

    private val KEY = Regex("^//\\s*@(\\S+)\\s*(.*)$")

    fun parse(code: String): Meta {
        var name: String? = null
        var desc: String? = null
        var version: String? = null
        val matches = ArrayList<String>()
        val includes = ArrayList<String>()
        val excludes = ArrayList<String>()
        val grants = ArrayList<String>()
        var runAt = Extension.RUN_END

        // everything up to the first line of actual code
        val head = code.lineSequence()
            .take(150)
            .takeWhile { l ->
                val t = l.trimStart()
                t.isEmpty() || t.startsWith("//") || t.startsWith("/*") || t.startsWith("*") ||
                    t.startsWith("#")
            }

        for (line in head) {
            val m = KEY.find(line.trim()) ?: continue
            val key = m.groupValues[1].lowercase()
            val v = m.groupValues[2].trim()
            when (key) {
                "name" -> if (name == null) name = v
                "description" -> if (desc == null) desc = v
                "version" -> if (version == null) version = v
                "match" -> if (v.isNotEmpty()) matches.add(v)
                "include" -> if (v.isNotEmpty()) includes.add(v)
                "exclude" -> if (v.isNotEmpty()) excludes.add(v)
                "grant" -> if (v.isNotEmpty()) grants.add(v)
                "run-at" -> runAt = if (v.contains("start")) Extension.RUN_START else Extension.RUN_END
            }
        }
        return Meta(name, desc, version, matches, includes, excludes, runAt, grants)
    }
}
