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

    /**
     * Pinned to a tag, as asked.
     *
     * A tag is immutable, so jsDelivr caches it once and serves it from the edge
     * for good - the index and the scripts both come from the CDN and neither can
     * lag behind a publish. The cost is honest: a tag is a fixed point, so adding
     * an extension means pushing a new tag and bumping this constant, which is an
     * app release. The raw host stays as the fallback so the store still works if
     * the tag is missing or the CDN is unreachable.
     */
    const val DEFAULT_BASE =
        "https://cdn.jsdelivr.net/gh/vasudev-work/Ekant-Browser@store-v1/extensions/"
    /** Same pinned tag for the scripts themselves. */
    const val SCRIPT_BASE =
        "https://cdn.jsdelivr.net/gh/vasudev-work/Ekant-Browser@store-v1/extensions/"
    /** If the tag is missing or the CDN is down, the live branch. */
    const val FALLBACK_BASE =
        "https://raw.githubusercontent.com/vasudev-work/Ekant-Browser/main/extensions/"

    /**
     * The store's public key. A list is only trusted when it carries a signature
     * that this key verifies, so a changed index in the repository - by anyone
     * without the private key - is refused instead of installed.
     */
    private const val STORE_PUBKEY_B64 =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEy+pYBu96v2EwW82f3h72ZnlBy0OekSZs9hXzyYJsV4d13VXy4NXxtLrtVPnwPhYX+VK54F6C48ttHZUo4JTFmQ=="

    /** True when [sigB64] is a valid SHA-256 ECDSA signature over [body]. */
    private fun signatureOk(body: String, sigB64: String): Boolean = try {
        val keyBytes = android.util.Base64.decode(STORE_PUBKEY_B64, android.util.Base64.NO_WRAP)
        val pub = java.security.KeyFactory.getInstance("EC")
            .generatePublic(java.security.spec.X509EncodedKeySpec(keyBytes))
        val sig = java.security.Signature.getInstance("SHA256withECDSA")
        sig.initVerify(pub)
        sig.update(body.toByteArray(Charsets.UTF_8))
        sig.verify(android.util.Base64.decode(sigB64.trim(), android.util.Base64.NO_WRAP))
    } catch (_: Exception) { false }

    data class Remote(
        val id: String,
        val name: String,
        val desc: String,
        val pattern: String,
        val file: String,
        val version: String,
        val sha256: String = ""
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
                val sig = try { http(join(base, "index.json.sig")) } catch (_: Exception) { "" }
                if (sig.isBlank() || !signatureOk(body, sig)) {
                    cb(null, "Store signature is not valid - the list was refused")
                    return@Thread
                }
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
                            e.optString("version", "1.0"),
                            e.optString("sha256", "")
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
    fun fetchScript(base: String, file: String, cb: (String?, String?) -> Unit) =
        fetchScript(base, file, null, cb)

    /**
     * Downloads a script and checks it against the hash the index published for
     * it. A file that changed under us - a tampered repository, a proxy rewriting
     * the response - is refused rather than installed.
     */
    fun fetchScript(base: String, file: String, expectedSha: String?,
                    cb: (String?, String?) -> Unit) {
        Thread {
            try {
                // scripts come from the fast CDN; the hash below is what makes
                // that safe, not the host
                var text = ""
                for (host in listOf(SCRIPT_BASE, base)) {
                    try {
                        val got = http(join(host, file))
                        if (got.isNotBlank()) { text = got; break }
                    } catch (_: Exception) { }
                }
                if (text.isBlank()) { cb(null, "Empty script"); return@Thread }
                if (!expectedSha.isNullOrBlank()) {
                    val got = sha256(text)
                    if (!got.equals(expectedSha, ignoreCase = true)) {
                        cb(null, "Checksum mismatch - the script was not installed")
                        return@Thread
                    }
                }
                cb(text, null)
            } catch (e: Exception) {
                cb(null, e.message ?: "Download failed")
            }
        }.start()
    }

    /** Lower-case hex SHA-256 of the text as it arrived, UTF-8. */
    fun sha256(text: String): String = try {
        val d = java.security.MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
        d.joinToString("") { String.format("%02x", it) }
    } catch (_: Exception) { "" }
}
