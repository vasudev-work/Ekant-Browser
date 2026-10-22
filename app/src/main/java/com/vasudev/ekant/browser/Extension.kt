package com.vasudev.ekant.browser

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
    val desc: String = ""
) {
    /** pattern supports '*' wildcards and comma-separated alternatives. */
    fun matches(url: String): Boolean {
        val p = pattern.trim()
        if (p.isEmpty() || p == "*") return true
        return p.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .any { wildcardMatch(it, url) }
    }

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
        .put("desc", desc)

    companion object {
        fun fromJson(o: JSONObject) = Extension(
            id = o.getString("id"),
            name = o.getString("name"),
            pattern = o.optString("pattern", "*"),
            code = o.optString("code", ""),
            enabled = o.optBoolean("enabled", true),
            builtIn = o.optBoolean("builtIn", false),
            desc = o.optString("desc", "")
        )

        fun new(name: String, pattern: String, code: String, desc: String = "") =
            Extension(UUID.randomUUID().toString(), name, pattern, code, true, false, desc)
    }
}
