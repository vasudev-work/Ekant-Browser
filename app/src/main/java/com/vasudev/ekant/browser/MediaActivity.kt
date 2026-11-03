package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.widget.LinearLayout

/** Media found on the current page, with download and copy actions. */
class MediaActivity : BaseSettingsActivity() {

    override val titleRes = R.string.m_media

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val list = MediaSniffer.all()
        b.section(MediaSniffer.host.ifBlank { getString(R.string.m_media) })
        if (list.isEmpty()) {
            b.note(getString(R.string.media_empty))
        } else {
            for (item in list) {
                val url = item.first
                val kind = item.second
                val card = b.card()
                b.valueRow(card, kind, url.take(46), null)
                b.divider(card)
                b.navRow(card, getString(R.string.media_download), null) { download(url) }
                b.divider(card)
                b.navRow(card, getString(R.string.media_copy), null) { copy(url) }
            }
        }
        b.note(getString(R.string.media_note))
    }

    private fun toast(s: CharSequence) {
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun download(url: String) {
        // a playlist is not a file: DownloadManager would save a text file with
        // segment URLs in it, so it goes through the HLS downloader instead
        if (url.substringBefore('?').lowercase().endsWith(".m3u8")) {
            val cookie = try {
                android.webkit.CookieManager.getInstance().getCookie(url)
            } catch (_: Exception) { null }
            toast(getString(R.string.md_started))
            HlsDownloader.download(this, url, cookie,
                url.substringAfter("://").substringBefore('/').ifBlank { "video" }.take(40)) { ok, info ->
                runOnUiThread {
                    toast(if (ok) getString(R.string.md_done, info) else getString(R.string.md_failed, info))
                }
            }
            return
        }
        try {
            val r = DownloadManager.Request(Uri.parse(url))
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, null, null))
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(r)
            toast(getString(R.string.ctx_downloading))
        } catch (_: Exception) {
            toast(getString(R.string.ctx_failed))
        }
    }

    private fun copy(url: String) {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Aekant", url))
            toast(getString(R.string.ctx_copied))
        } catch (_: Exception) { }
    }
}
