package com.vasudev.ekant.browser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** The Pause / Resume / Cancel buttons on a download notification. */
class DownloadActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        if (id < 0) return
        when (intent.getStringExtra("action")) {
            "pause" -> FastDownloader.pause(id)
            "resume" -> FastDownloader.resume(context, id)
            "cancel" -> FastDownloader.cancel(context, id)
        }
    }
}
