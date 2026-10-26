package com.aekantkit

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream

/**
 * EasyList / uBlock-syntax filter engine.
 *
 * Network rules run in shouldInterceptRequest; cosmetic rules are injected as CSS.
 * Bundled lists come from the EasyList project (easylist/easylist, EasyPrivacy).
 */
object AdFilterEngine {

    @Volatile var enabled: Boolean = true
    @Volatile private var ready: Boolean = false
    @Volatile private var appCtx: android.content.Context? = null
    @Volatile var blockedCount: Int = 0
    @Volatile var savedBytes: Long = 0L
    @Volatile private var allowSet: Set<String> = emptySet()
    private val started = AtomicBoolean(false)

    private val blockHosts = HashSet<String>()
    private val allowHosts = HashSet<String>()
    private val blockSubs = ArrayList<String>()
    private val allowSubs = ArrayList<String>()
    private val cosmeticGlobal = StringBuilder()
    private val cosmeticByDomain = HashMap<String, StringBuilder>()

    private const val MAX_HOSTS = 200000
    private const val MAX_ALLOW_HOSTS = 40000
    private const val MAX_SUBS = 6000
    private const val MAX_ALLOW_SUBS = 2000
    private const val MAX_GLOBAL_CSS = 200000

    fun ensureLoaded(context: Context) {
        appCtx = context.applicationContext
        loadStats()
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

    private fun openAsset(context: Context, name: String): java.io.InputStream? = try {
        GZIPInputStream(context.assets.open(name))
    } catch (_: Exception) {
        try { context.assets.open(name.removeSuffix(".gz")) } catch (_: Exception) { null }
    }

    private fun loadCosmetic(context: Context) {
        try {
            openAsset(context, "easylist_cosmetic.txt.gz")?.bufferedReader()?.use { r ->
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
            openAsset(context, "easylist_net.txt.gz")?.bufferedReader()?.use { r ->
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
                        if (isAllow) { if (allowHosts.size < MAX_ALLOW_HOSTS) allowHosts.add(d) }
                        else { if (blockHosts.size < MAX_HOSTS) blockHosts.add(d) }
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

    private fun prefs() = appCtx?.getSharedPreferences("aekantkit", Context.MODE_PRIVATE)

    private fun loadStats() {
        try {
            val p = prefs() ?: return
            blockedCount = p.getInt("ab_count", 0)
            savedBytes = p.getLong("ab_bytes", 0L)
            allowSet = (p.getString("ab_allow", "") ?: "").split("\n")
                .map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toHashSet()
        } catch (_: Exception) { }
    }

    /** Sites where blocking is switched off. */
    fun allow(host: String, on: Boolean) {
        val h = host.trim().lowercase()
        if (h.isEmpty()) return
        val s = allowSet.toMutableSet()
        if (on) s.add(h) else s.remove(h)
        allowSet = s
        try { prefs()?.edit()?.putString("ab_allow", s.joinToString("\n"))?.apply() } catch (_: Exception) { }
    }

    fun allowList(): List<String> = allowSet.sorted()
    fun isAllowed(host: String): Boolean = allowSet.contains(host.trim().lowercase())

    /** Called for every request the engine blocks. */
    fun recordBlocked() {
        blockedCount++
        savedBytes += 20000L
        if (blockedCount % 10 == 0) saveStats()
    }

    fun saveStats() {
        try { prefs()?.edit()?.putInt("ab_count", blockedCount)?.putLong("ab_bytes", savedBytes)?.apply() } catch (_: Exception) { }
    }

    fun resetStats() {
        blockedCount = 0
        savedBytes = 0L
        saveStats()
    }

    fun isBlocked(url: String): Boolean {
        if (!enabled || !ready) return false
        val u = url.lowercase()
        val host = hostOf(u) ?: return false
        if (allowSet.isNotEmpty()) {
            var ah = host
            while (true) {
                if (allowSet.contains(ah)) return false
                val ai = ah.indexOf('.')
                if (ai < 0) break
                ah = ah.substring(ai + 1)
            }
        }
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
