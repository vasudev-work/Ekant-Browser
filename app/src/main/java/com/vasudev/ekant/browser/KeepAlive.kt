package com.vasudev.ekant.browser

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays a near-silent loop so the system keeps the process alive and does not
 * throttle background audio (the "inaudible keep-alive" trick).
 */
object KeepAlive {

    private var player: MediaPlayer? = null

    fun start(c: Context) {
        if (player != null) return
        try {
            val tmp = java.io.File(c.cacheDir, "silent.wav")
            if (!tmp.exists() || tmp.length() == 0L) {
                c.assets.open("silent.wav").use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
            }
            val p = MediaPlayer()
            p.setDataSource(tmp.absolutePath)
            p.isLooping = true
            p.setVolume(0.001f, 0.001f)
            p.prepare()
            p.start()
            player = p
        } catch (_: Exception) { }
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) { }
        try { player?.release() } catch (_: Exception) { }
        player = null
    }

    fun running(): Boolean = player != null
}
