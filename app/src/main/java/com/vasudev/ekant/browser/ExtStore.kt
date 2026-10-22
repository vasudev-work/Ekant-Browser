package com.vasudev.ekant.browser

import org.json.JSONArray
import org.json.JSONObject

/**
 * Downloads extensions published in a GitHub repository.
 *
 * The repository holds an `extensions/` folder with an `index.json` listing every
 * extension and one .js file per extension. Nothing is bundled: the app fetches
 * the list on demand and installs the scripts the user picks.
 */
object ExtStore {

    const val DEFAULT_BASE =
        "https://raw.githubusercontent.com/vasudev-work/Ekant-Browser/main/extensions/"

    data class Remote(
        val id: String,
        val name: String,
        val desc: String,
        val pattern: String,
        val file: String,
        val version: String
    )

    private fun join(base: String, file: String): String {
        val b = if (base.endsWith("/")) base else base + "/"
        return b + file
    }

    private fun http(u: String): String {
        val c = java.net.URL(u).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 10000
        c.readTimeout = 15000
        c.setRequestProperty("User-Agent", "Aekant")
        c.setRequestProperty("Accept", "application/json, text/plain, */*")
        try {
            return c.inputStream.bufferedReader().readText()
        } finally {
            try { c.disconnect() } catch (_: Exception) { }
        }
    }

    /** Reads index.json. Callback runs on the calling thread's background worker. */
    fun fetchIndex(base: String, cb: (List<Remote>?, String?) -> Unit) {
        Thread {
            try {
                val body = http(join(base, "index.json"))
                val arr: JSONArray = try {
                    val o = JSONObject(body)
                    o.optJSONArray("extensions") ?: JSONArray()
                } catch (_: Exception) {
                    JSONArray(body)
                }
                val out = ArrayList<Remote>()
                for (i in 0 until arr.length()) {
                    val e = arr.optJSONObject(i) ?: continue
                    val file = e.optString("file", "")
                    if (file.isBlank()) continue
                    out.add(
                        Remote(
                            e.optString("id", "ext-" + i),
                            e.optString("name", "Extension"),
                            e.optString("desc", ""),
                            e.optString("pattern", "*"),
                            file,
                            e.optString("version", "1.0")
                        )
                    )
                }
                cb(out, null)
            } catch (e: Exception) {
                cb(null, e.message ?: "Could not reach the store")
            }
        }.start()
    }

    /** Fetches one script and hands back its text. */
    fun fetchScript(base: String, file: String, cb: (String?, String?) -> Unit) {
        Thread {
            try {
                val text = http(join(base, file))
                if (text.isBlank()) cb(null, "Empty script") else cb(text, null)
            } catch (e: Exception) {
                cb(null, e.message ?: "Download failed")
            }
        }.start()
    }
}
