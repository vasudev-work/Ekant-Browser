package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.widget.LinearLayout

/** Storage summary and a breakdown of downloaded files by type. */
class FilesActivity : BaseSettingsActivity() {

    override val titleRes = R.string.fl_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.fl_storage))
        val s = b.card()
        b.valueRow(s, getString(R.string.fl_available), available(), null)
        b.divider(s)
        b.valueRow(s, getString(R.string.fl_total), total(), null)
        b.note(getString(R.string.fl_note))

        val counts = counts()
        b.section(getString(R.string.fl_category))
        val c = b.card()
        b.valueRow(c, getString(R.string.fl_images), "" + (counts["image"] ?: 0), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.fl_videos), "" + (counts["video"] ?: 0), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.fl_audio), "" + (counts["audio"] ?: 0), null)
        b.divider(c)
        b.valueRow(c, getString(R.string.fl_docs), "" + (counts["doc"] ?: 0), null)

        b.section(getString(R.string.s_downloads))
        val d = b.card()
        b.navRow(d, getString(R.string.s_downloads), null) { openDownloads() }
    }

    private fun openDownloads() {
        try {
            startActivity(android.content.Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS))
        } catch (_: Exception) { }
    }

    private fun fmt(bytes: Long): String {
        val gb = bytes.toDouble() / (1024 * 1024 * 1024)
        return if (gb >= 1) String.format(java.util.Locale.getDefault(), "%.1f GB", gb)
        else String.format(java.util.Locale.getDefault(), "%.0f MB", bytes.toDouble() / (1024 * 1024))
    }

    private fun available(): String = try {
        val st = StatFs(Environment.getExternalStorageDirectory().path)
        fmt(st.availableBytes)
    } catch (_: Exception) { "?" }

    private fun total(): String = try {
        val st = StatFs(Environment.getExternalStorageDirectory().path)
        fmt(st.totalBytes)
    } catch (_: Exception) { "?" }

    private fun counts(): Map<String, Int> {
        val out = HashMap<String, Int>()
        try {
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.query(DownloadManager.Query()).use { c ->
                val idx = c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)
                if (idx < 0) return out
                while (c.moveToNext()) {
                    val mime = (c.getString(idx) ?: "").lowercase()
                    val key = when {
                        mime.startsWith("image") -> "image"
                        mime.startsWith("video") -> "video"
                        mime.startsWith("audio") -> "audio"
                        else -> "doc"
                    }
                    out[key] = (out[key] ?: 0) + 1
                }
            }
        } catch (_: Exception) { }
        return out
    }
}
