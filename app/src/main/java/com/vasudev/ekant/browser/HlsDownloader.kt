package com.vasudev.ekant.browser

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * An HLS downloader.
 *
 * A .m3u8 is not a file, it is a list. This fetches the playlist, picks the best
 * variant, walks the segments, decrypts the ones behind AES-128, and writes them
 * out as one file - the same job the Chrome extension does with chrome.downloads.
 *
 * Deliberately sequential-ish: four segments at a time. A phone downloading eight
 * at once finishes no sooner and burns more data.
 */
object HlsDownloader {

    private val pool = Executors.newFixedThreadPool(4)

    fun download(ctx: Context, url: String, cookie: String?, name: String,
                 cb: (Boolean, String) -> Unit) {
        Thread {
            var out: File? = null
            try {
                val playlist = fetch(url, cookie)
                val text = playlist.toString(Charsets.UTF_8)
                if (!text.contains("#EXTM3U")) { cb(false, "not a playlist"); return@Thread }

                // a master playlist points at variants; take the highest bandwidth
                val mediaUrl = if (text.contains("#EXT-X-STREAM-INF")) {
                    val lines = text.lines()
                    var best = ""; var bestBw = -1
                    for (i in lines.indices) {
                        val l = lines[i]
                        if (!l.startsWith("#EXT-X-STREAM-INF")) continue
                        val bw = Regex("BANDWIDTH=(\\d+)").find(l)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                        val next = lines.getOrNull(i + 1)?.trim() ?: continue
                        if (next.isNotEmpty() && !next.startsWith("#") && bw > bestBw) { bestBw = bw; best = next }
                    }
                    if (best.isBlank()) url else resolve(url, best)
                } else url

                val media = fetch(mediaUrl, cookie).toString(Charsets.UTF_8)
                val base = mediaUrl.substringBeforeLast('/') + "/"
                val key = parseKey(media, base)
                val iv = parseIv(media) ?: ByteArray(16)
                val segments = media.lines().map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { resolve(mediaUrl, it) }
                if (segments.isEmpty()) { cb(false, "no segments"); return@Thread }

                val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: ctx.cacheDir
                out = File(dir, safeName(name) + ".ts")
                if (out.exists()) out.delete()
                FileOutputStream(out).use { fos ->
                    val done = AtomicInteger(0)
                    for (chunk in segments.chunked(4)) {
                        val parts = chunk.map { seg ->
                            pool.submit<ByteArray> {
                                var b = fetch(seg, cookie)
                                if (key != null) b = decrypt(b, key, iv)
                                done.incrementAndGet()
                                b
                            }
                        }
                        for (f in parts) fos.write(f.get())
                    }
                }

                val file = out
                if (file != null && file.exists() && file.length() > 0) {
                    publish(ctx, file, safeName(name) + ".ts")
                    cb(true, file.absolutePath)
                } else cb(false, "empty file")
            } catch (e: Exception) {
                cb(false, e.message ?: "download failed")
            }
        }.start()
    }

    private fun parseKey(media: String, base: String): ByteArray? {
        val m = Regex("#EXT-X-KEY:METHOD=AES-128,URI=\"([^\"]+)\"").find(media) ?: return null
        return try { fetch(resolve(base, m.groupValues[1]), null) } catch (_: Exception) { null }
    }

    private fun parseIv(media: String): ByteArray? {
        val m = Regex("IV=0x([0-9A-Fa-f]+)").find(media) ?: return null
        val hex = m.groupValues[1]
        val b = ByteArray(16)
        val raw = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        System.arraycopy(raw, 0, b, 16 - raw.size, raw.size)
        return b
    }

    private fun decrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray = try {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        c.doFinal(data)
    } catch (_: Exception) { data }

    /** Into Downloads where the system allows it, otherwise beside the app. */
    private fun publish(ctx: Context, file: File, name: String) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp2t")
                }
                val uri: Uri? = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    ctx.contentResolver.openOutputStream(uri)?.use { o -> file.inputStream().use { it.copyTo(o) } }
                    file.delete()
                    return
                }
            }
            val dl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dest = File(dl, name)
            file.copyTo(dest, overwrite = true)
        } catch (_: Exception) { }
    }

    private fun fetch(url: String, cookie: String?): ByteArray {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 30000
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
        if (!cookie.isNullOrBlank()) c.setRequestProperty("Cookie", cookie)
        return c.inputStream.use { it.readBytes() }
    }

    private fun resolve(base: String, ref: String): String {
        if (ref.startsWith("http")) return ref
        val b = base.substringBeforeLast('/')
        return if (ref.startsWith("/")) {
            val root = base.substringBefore("://").let { scheme ->
                scheme + "://" + base.substringAfter("://").substringBefore('/')
            }
            root + ref
        } else b + "/" + ref
    }

    private fun safeName(n: String): String {
        val s = n.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        val h = MessageDigest.getInstance("MD5").digest(n.toByteArray()).joinToString("") { "%02x".format(it) }.take(6)
        return (if (s.isBlank()) "video" else s.take(40)) + "-" + h
    }
}
