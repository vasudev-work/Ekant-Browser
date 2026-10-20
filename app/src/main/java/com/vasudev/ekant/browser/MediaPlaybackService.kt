package com.vasudev.ekant.browser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

/** Keeps audio/video playing while the app is in the background or the screen is off. */
class MediaPlaybackService : Service() {

    private var wake: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val stop = PendingIntent.getService(this, 1,
            Intent(this, MediaPlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        running = true
        val notif: Notification = Notification.Builder(this, CHANNEL)
            .setContentTitle(getString(R.string.bg_playing))
            .setContentText(getString(R.string.bg_playing_sum))
            .setSmallIcon(R.drawable.ic_aekant_launcher)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(
                Icon.createWithResource(this, R.drawable.ic_aekant_launcher),
                getString(R.string.bg_stop), stop).build())
            .build()
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(1, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(1, notif)
            }
        } catch (_: Exception) {
            try { startForeground(1, notif) } catch (_: Exception) { }
        }
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "aekant:media")
            wake?.acquire()
        } catch (_: Exception) { }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        return START_STICKY
    }

    override fun onDestroy() {
        try { wake?.let { if (it.isHeld) it.release() } } catch (_: Exception) { }
        wake = null
        running = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(CHANNEL) == null) {
                    nm.createNotificationChannel(NotificationChannel(
                        CHANNEL, getString(R.string.bg_channel), NotificationManager.IMPORTANCE_LOW))
                }
            } catch (_: Exception) { }
        }
    }

    companion object {
        @Volatile var running: Boolean = false
        const val CHANNEL = "aekant_media"
        const val ACTION_STOP = "com.vasudev.ekant.browser.STOP_MEDIA"
        fun start(c: Context) {
            try {
                val i = Intent(c, MediaPlaybackService::class.java)
                if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i) else c.startService(i)
            } catch (_: Exception) { }
        }
        fun stop(c: Context) {
            try { c.stopService(Intent(c, MediaPlaybackService::class.java)) } catch (_: Exception) { }
        }
    }
}
