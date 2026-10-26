package com.aekantkit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicLong

/**
 * A multi-part downloader.
 *
 * The system DownloadManager gives you one connection per file. This splits a
 * file into four ranges and fetches them at the same time, which is several times
 * faster on a fast line, and it can pause and carry on later without starting
 * over - each part remembers how far it got.
 *
 * It only engages when the server actually offers byte ranges and the file is
 * worth splitting; otherwise the caller falls back to DownloadManager.
 */
object FastDownloader {

    private const val PARTS = 4
    private const val MIN_SIZE = 1_000_000L
    private const val UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"

    private const val CHANNEL = "aekant_downloads"
    private const val NOTIF_ID = 4801

    class Job(
        val id: Int,
        val url: String,
        val name: String,
        val mime: String?,
        val cookie: String?
    ) {
        @Volatile var cancelled = false
        @Volatile var paused = false
        @Volatile var finished = false
        val done = AtomicLong(0L)
        var total = 0L
        var file: File? = null
        var parts: LongArray = LongArray(PARTS)
        var ends: LongArray = LongArray(PARTS)
        val connections = java.util.Collections.synchronizedList(ArrayList<HttpURLConnection>())
    }

    private val jobs = java.util.concurrent.ConcurrentHashMap<Int, Job>()
    private var nextId = 1

    /** True when the file can actually be split. Cheap: one ranged request. */
    fun probe(url: String, cookie: String?): Long {
        var c: HttpURLConnection? = null
        try {
            c = open(url, cookie, "bytes=0-0")
            if (c.responseCode != 206) return -1
            val cr = c.getHeaderField("Content-Range") ?: return -1
            val total = cr.substringAfter('/').trim().toLongOrNull() ?: return -1
            return if (total >= MIN_SIZE) total else -1
        } catch (_: Exception) {
            return -1
        } finally {
            try { c?.disconnect() } catch (_: Exception) { }
        }
    }

    fun start(ctx: Context, url: String, name: String, mime: String?, cookie: String?, total: Long) {
        val job = Job(nextId++, url, name, mime, cookie)
        job.total = total
        jobs[job.id] = job
        Thread { run(ctx.applicationContext, job) }.start()
    }

    fun pause(id: Int) {
        jobs[id]?.let {
            it.paused = true
            for (c in ArrayList(it.connections)) { try { c.disconnect() } catch (_: Exception) { } }
            it.connections.clear()
        }
    }

    fun resume(ctx: Context, id: Int) {
        val job = jobs[id] ?: return
        job.paused = false
        Thread { run(ctx.applicationContext, job) }.start()
    }

    fun cancel(ctx: Context, id: Int) {
        val job = jobs[id] ?: return
        job.cancelled = true
        job.paused = false
        for (c in ArrayList(job.connections)) { try { c.disconnect() } catch (_: Exception) { } }
        job.connections.clear()
        try { job.file?.delete() } catch (_: Exception) { }
        jobs.remove(id)
        try { manager(ctx).cancel(NOTIF_ID) } catch (_: Exception) { }
    }

    private fun run(ctx: Context, job: Job) {
        try {
            if (job.file == null) {
                val dir = File(ctx.cacheDir, "fastdl")
                if (!dir.exists()) dir.mkdirs()
                val target = File(dir, job.name)
                job.file = target
                val part = job.total / PARTS
                for (i in 0 until PARTS) {
                    job.parts[i] = i * part
                    job.ends[i] = if (i == PARTS - 1) job.total - 1 else (i * part + part - 1)
                }
                RandomAccessFile(target, "rw").use { it.setLength(job.total) }
                job.done.set(0L)
            }

            notify(ctx, job, "Downloading " + job.name, false)

            val workers = (0 until PARTS).map { i ->
                Thread {
                    var pos = job.parts[i]
                    val end = job.ends[i]
                    while (pos <= end && !job.cancelled && !job.paused) {
                        var c: HttpURLConnection? = null
                        try {
                            c = open(job.url, job.cookie, "bytes=" + pos + "-" + end)
                            job.connections.add(c)
                            if (c.responseCode !in 200..299) throw IOException("HTTP " + c.responseCode)
                            c.inputStream.use { ins ->
                                val buf = ByteArray(64 * 1024)
                                while (pos <= end && !job.cancelled && !job.paused) {
                                    val n = ins.read(buf)
                                    if (n <= 0) break
                                    RandomAccessFile(job.file!!, "rw").use { raf ->
                                        raf.seek(pos)
                                        raf.write(buf, 0, n)
                                    }
                                    pos += n
                                    job.parts[i] = pos
                                    job.done.addAndGet(n.toLong())
                                }
                            }
                        } catch (_: Exception) {
                            if (job.cancelled || job.paused) break
                            try { Thread.sleep(400) } catch (_: InterruptedException) { break }
                        } finally {
                            c?.let { job.connections.remove(it) }
                            try { c?.disconnect() } catch (_: Exception) { }
                        }
                    }
                }.also { it.start() }
            }
            for (t in workers) t.join()

            if (job.cancelled) return
            if (job.paused) { notify(ctx, job, "Paused " + job.name, true); return }
            if (job.done.get() < job.total) { notify(ctx, job, "Incomplete: " + job.name, true); return }

            job.finished = true
            val saved = publish(ctx, job)
            jobs.remove(job.id)
            done(ctx, job, saved)
        } catch (_: Exception) {
            notify(ctx, job, "Download failed", true)
        }
    }

    private fun open(url: String, cookie: String?, range: String?): HttpURLConnection {
        val c = java.net.URL(url).openConnection() as HttpURLConnection
        c.instanceFollowRedirects = true
        c.connectTimeout = 15000
        c.readTimeout = 30000
        c.setRequestProperty("User-Agent", UA)
        if (!cookie.isNullOrBlank()) c.setRequestProperty("Cookie", cookie)
        if (range != null) c.setRequestProperty("Range", range)
        return c
    }

    private fun publish(ctx: Context, job: Job): String {
        val src = job.file ?: return ""
        val mime = job.mime ?: "application/octet-stream"
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, job.name)
                    put(MediaStore.Downloads.MIME_TYPE, mime)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = ctx.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri == null) return ""
                resolver.openOutputStream(uri)?.use { out -> src.inputStream().use { it.copyTo(out) } }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                src.delete()
                "Downloads/" + job.name
            } else {
                val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return src.absolutePath
                if (!dir.exists()) dir.mkdirs()
                val out = File(dir, job.name)
                src.inputStream().use { ins -> out.outputStream().use { ins.copyTo(it) } }
                src.delete()
                out.absolutePath
            }
        } catch (_: Exception) {
            src.absolutePath
        }
    }

    @Suppress("DEPRECATION")
    private fun builder(ctx: Context): Notification.Builder =
        if (Build.VERSION.SDK_INT >= 26) Notification.Builder(ctx, CHANNEL)
        else Notification.Builder(ctx)

    private fun manager(ctx: Context): NotificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun channel(ctx: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val m = manager(ctx)
        if (m.getNotificationChannel(CHANNEL) != null) return
        m.createNotificationChannel(
            NotificationChannel(CHANNEL, "Downloads", NotificationManager.IMPORTANCE_LOW))
    }

    private fun notify(ctx: Context, job: Job, text: String, withActions: Boolean) {
        try {
            channel(ctx)
            val open = PendingIntent.getActivity(ctx, 0,
                Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val b = builder(ctx)
            b.setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Download")
                .setContentText(text)
                .setContentIntent(open)
                .setOnlyAlertOnce(true)
                .setOngoing(!withActions)
            if (job.total > 0) {
                b.setProgress(100, ((job.done.get() * 100) / job.total).toInt(), false)
                b.setSubText((job.done.get() / 1048576).toString() + " / " +
                    (job.total / 1048576) + " MB")
            }
            if (withActions) {
                val resume = PendingIntent.getBroadcast(ctx, job.id,
                    Intent(ctx, DownloadActionReceiver::class.java)
                        .putExtra("action", "resume").putExtra("id", job.id),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val cancel = PendingIntent.getBroadcast(ctx, job.id + 9000,
                    Intent(ctx, DownloadActionReceiver::class.java)
                        .putExtra("action", "cancel").putExtra("id", job.id),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (job.paused) b.addAction(Notification.Action.Builder(0, "Resume", resume).build())
                b.addAction(Notification.Action.Builder(0, "Cancel", cancel).build())
            } else {
                val pause = PendingIntent.getBroadcast(ctx, job.id,
                    Intent(ctx, DownloadActionReceiver::class.java)
                        .putExtra("action", "pause").putExtra("id", job.id),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                b.addAction(Notification.Action.Builder(0, "Pause", pause).build())
            }
            manager(ctx).notify(NOTIF_ID, b.build())
        } catch (_: Exception) { }
    }

    private fun done(ctx: Context, job: Job, where: String) {
        try {
            channel(ctx)
            val open = PendingIntent.getActivity(ctx, 0,
                Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val b = builder(ctx)
            b.setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(job.name)
                .setContentText("Saved to " + where)
                .setContentIntent(open)
                .setAutoCancel(true)
            manager(ctx).notify(NOTIF_ID, b.build())
        } catch (_: Exception) { }
    }
}
