package com.vasudev.ekant.browser

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Pulls fresh copies of the filter lists and keeps them in the app's own files
 * directory, which AdFilterEngine prefers over the copies baked into the APK.
 *
 * Runs weekly on an unmetered connection, so it costs the user no mobile data.
 */
class FilterRefreshWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    companion object {
        const val WORK = "filter-refresh"
        const val KEY_BASE = "base"
        const val DIR = "filters"
        val FILES = arrayOf(
            "easylist_net.txt.gz",
            "easyprivacy_net.txt.gz",
            "hosts_extra.txt.gz",
            "easylist_cosmetic.txt.gz"
        )
    }

    override fun doWork(): Result {
        val base = inputData.getString(KEY_BASE) ?: return Result.failure()
        val dir = File(applicationContext.filesDir, DIR).apply { mkdirs() }
        var got = 0
        for (name in FILES) {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(base + name).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 40000
                    instanceFollowRedirects = true
                }
                if (conn.responseCode != 200) continue
                // write to a temporary file first, so a half download never
                // replaces a good list
                val tmp = File(dir, "$name.tmp")
                conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
                if (tmp.length() > 1024) {
                    val dest = File(dir, name)
                    if (dest.exists()) dest.delete()
                    tmp.renameTo(dest)
                    got++
                } else tmp.delete()
            } catch (_: Exception) {
            } finally {
                try { conn?.disconnect() } catch (_: Exception) { }
            }
        }
        return if (got > 0) Result.success() else Result.retry()
    }
}
