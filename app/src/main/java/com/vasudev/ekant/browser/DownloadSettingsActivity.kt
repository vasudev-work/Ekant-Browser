package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.Intent
import android.os.Environment
import android.widget.LinearLayout

/** Where downloads go and how they behave. */
class DownloadSettingsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.dls_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.dls_title))
        val c = b.card()
        b.valueRow(c, getString(R.string.dls_save_to), savePath(), null)
        b.divider(c)
        b.switchRow(c, getString(R.string.dl_ask), getString(R.string.dl_ask_sum),
            prefs.getBoolean("ask_download", false)) { _, on ->
            prefs.edit().putBoolean("ask_download", on).apply()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.dls_background), getString(R.string.dls_background_sum),
            prefs.getBoolean("bg_download", true)) { _, on ->
            prefs.edit().putBoolean("bg_download", on).apply()
        }
        b.note(getString(R.string.dls_note))

        b.section(getString(R.string.s_downloads))
        val d = b.card()
        b.navRow(d, getString(R.string.s_downloads), null,
            android.view.View.OnClickListener {
                try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) } catch (_: Exception) { }
            })
    }

    private fun savePath(): String = try {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    } catch (_: Exception) { "Downloads" }
}
