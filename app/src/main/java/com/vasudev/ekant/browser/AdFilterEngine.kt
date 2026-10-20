package com.vasudev.ekant.browser

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

/**
 * EasyList / uBlock-syntax filter engine.
 *
 * Network rules run in shouldInterceptRequest; cosmetic rules are injected as CSS.
 * Bundled lists come from the EasyList project (easylist/easylist, EasyPrivacy).
 */
object AdFilterEngine {

    @Volatile var enabled: Boolean = true
    @Volatile private var ready: Boolean = false
    private val started = AtomicBoolean(false)

    private val blockHosts = HashSet<String>()
    private val allowHosts = HashSet<String>()
    private val blockSubs = ArrayList<String>()
    private val allowSubs = ArrayList<String>()
    private val cosmeticGlobal = StringBuilder()
    private val cosmeticByDomain = HashMap<String, StringBuilder>()

    private const val MAX_SUBS = 6000
    private const val MAX_ALLOW_SUBS = 2000
    private const val MAX_GLOBAL_CSS = 200000

    fun ensureLoaded(context: Context) {
        if (!started.compareAndSet(false, true)) return
        Thread {
            try {
                loadCosmetic(context)
                loadNetwork(context)
            } catch (_: Exception) {
            }
            ready = true
        }.start()
    }

    private fun loadCosmetic(context: Context) {
        try {
            context.assets.open("easylist_cosmetic.txt").bufferedReader().use { r ->
                r.forEachLine { raw ->
                    val line = raw.trim()
                    if (line.isEmpty()) return@forEachLine
                    val i = line.indexOf("##")
                    if (i < 0) return@forEachLine
                    val sel = line.substring(i + 2)
                    if (sel.isEmpty() || sel.contains('{') || sel.contains('}')) return@forEachLine
                    if (sel.contains(":has-text") || sel.contains(":style(") || sel.contains(":matches-") ||
                        sel.contains(":upward") || sel.contains(":remove(") || sel.contains(":xpath") ||
                        sel.contains(":-abp-")) return@forEachLine
                    val domains = line.substring(0, i)
                    if (domains.isEmpty()) {
                        if (cosmeticGlobal.length < MAX_GLOBAL_CSS) cosmeticGlobal.append(sel).append(',')
                    } else {
                        for (d in domains.split(',')) {
                            val k = d.trim()
                            if (k.isEmpty() || k.startsWith("~")) continue
                            val sb = cosmeticByDomain.getOrPut(k) { StringBuilder() }
                            if (sb.length < 15000) sb.append(sel).append(',')
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun loadNetwork(context: Context) {
        try {
            context.assets.open("easylist_net.txt").bufferedReader().use { r ->
                r.forEachLine { raw ->
                    val line = raw.trim()
                    if (line.isEmpty()) return@forEachLine
                    val isAllow = line.startsWith("@@")
                    var body = if (isAllow) line.substring(2) else line
                    if (body.contains('$')) body = body.substringBefore('$')
                    if (body.isEmpty()) return@forEachLine
                    if (body.startsWith("||")) {
                        var d = body.substring(2)
                        d = d.substringBefore('^').substringBefore('/')
                            .substringBefore('*').substringBefore('?').trim('.')
                        d = d.lowercase()
                        if (d.isEmpty() || !d.contains('.')) return@forEachLine
                        if (isAllow) allowHosts.add(d) else blockHosts.add(d)
                    } else {
                        if (body.startsWith("/") && body.endsWith("/")) return@forEachLine
                        if (body.contains('*') || body.contains('^')) return@forEachLine
                        if (body.startsWith("|")) body = body.substring(1)
                        if (body.length < 5) return@forEachLine
                        if (isAllow) { if (allowSubs.size < MAX_ALLOW_SUBS) allowSubs.add(body) }
                        else { if (blockSubs.size < MAX_SUBS) blockSubs.add(body) }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun isBlocked(url: String): Boolean {
        if (!enabled || !ready) return false
        val u = url.lowercase()
        val host = hostOf(u) ?: return false
        if (allowHosts.isNotEmpty() && matchesHost(allowHosts, host)) return false
        for (s in allowSubs) if (u.contains(s)) return false
        if (matchesHost(blockHosts, host)) return true
        for (s in blockSubs) if (u.contains(s)) return true
        return false
    }

    fun cosmeticCss(host: String): String {
        if (!enabled || !ready) return ""
        val sb = StringBuilder()
        if (cosmeticGlobal.isNotEmpty()) sb.append(cosmeticGlobal)
        var h = host.lowercase()
        while (true) {
            cosmeticByDomain[h]?.let { sb.append(it) }
            val i = h.indexOf('.')
            if (i < 0) break
            h = h.substring(i + 1)
        }
        if (sb.isEmpty()) return ""
        val all = sb.toString().trimEnd(',').split(',')
            .map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (all.isEmpty()) return ""
        // chunk so one bad selector cannot void the whole sheet
        val out = StringBuilder()
        var i = 0
        while (i < all.size) {
            val chunk = all.subList(i, minOf(i + 200, all.size))
            out.append(chunk.joinToString(",")).append("{display:none!important}\n")
            i += 200
        }
        return out.toString()
    }

    fun ruleCount(): Int = blockHosts.size + blockSubs.size

    private fun matchesHost(set: HashSet<String>, host: String): Boolean {
        if (set.isEmpty()) return false
        var h = host
        while (true) {
            if (set.contains(h)) return true
            val i = h.indexOf('.')
            if (i < 0) return false
            h = h.substring(i + 1)
        }
    }

    private fun hostOf(u: String): String? = try {
        java.net.URI(u).host?.lowercase()
    } catch (_: Exception) {
        null
    }
}
