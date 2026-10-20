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

    private fun download(url: String) {
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
