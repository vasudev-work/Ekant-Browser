package com.aekantkit

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
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        fun pi(id: Int, action: String) = PendingIntent.getService(this, id,
            Intent(this, MediaPlaybackService::class.java).setAction(action), flags)
        val stop = pi(1, ACTION_STOP)
        val prev = pi(2, ACTION_PREV)
        val toggle = pi(3, ACTION_TOGGLE)
        val next = pi(4, ACTION_NEXT)
        val icon = Icon.createWithResource(this, android.R.drawable.ic_media_play)
        running = true
        val notif: Notification = Notification.Builder(this, CHANNEL)
            .setContentTitle(TITLE)
            .setContentText(SUBTITLE)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(icon, "Previous", prev).build())
            .addAction(Notification.Action.Builder(icon, "Play / pause", toggle).build())
            .addAction(Notification.Action.Builder(icon, "Next", next).build())
            .addAction(Notification.Action.Builder(icon, "Stop", stop).build())
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
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_TOGGLE -> try { actions?.toggle() } catch (_: Exception) { }
            ACTION_NEXT -> try { actions?.next() } catch (_: Exception) { }
            ACTION_PREV -> try { actions?.prev() } catch (_: Exception) { }
        }
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
                        CHANNEL, "Playback", NotificationManager.IMPORTANCE_LOW))
                }
            } catch (_: Exception) { }
        }
    }

    /** Point these at your own player, or leave them null. */
    interface Actions {
        fun toggle()
        fun next()
        fun prev()
    }

    companion object {
        const val TITLE = "Playing in the background"
        const val SUBTITLE = "Tap to return to the app"
        @Volatile var actions: Actions? = null
        @Volatile var running: Boolean = false
        const val CHANNEL = "aekantkit_media"
        const val ACTION_STOP = "com.aekantkit.STOP"
        const val ACTION_TOGGLE = "com.aekantkit.TOGGLE"
        const val ACTION_NEXT = "com.aekantkit.NEXT"
        const val ACTION_PREV = "com.aekantkit.PREV"
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
