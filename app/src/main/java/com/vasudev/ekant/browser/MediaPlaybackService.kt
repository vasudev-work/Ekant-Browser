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
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        fun pi(id: Int, action: String) = PendingIntent.getService(this, id,
            Intent(this, MediaPlaybackService::class.java).setAction(action), flags)
        val stop = pi(1, ACTION_STOP)
        val prev = pi(2, ACTION_PREV)
        val toggle = pi(3, ACTION_TOGGLE)
        val next = pi(4, ACTION_NEXT)
        val icon = Icon.createWithResource(this, R.drawable.ic_aekant_launcher)
        running = true
        val notif: Notification = Notification.Builder(this, CHANNEL)
            .setContentTitle(getString(R.string.bg_playing))
            .setContentText(getString(R.string.bg_playing_sum))
            .setSmallIcon(R.drawable.ic_aekant_launcher)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(icon, getString(R.string.tts_prev), prev).build())
            .addAction(Notification.Action.Builder(icon, getString(R.string.tts_play), toggle).build())
            .addAction(Notification.Action.Builder(icon, getString(R.string.tts_next), next).build())
            .addAction(Notification.Action.Builder(icon, getString(R.string.bg_stop), stop).build())
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
        startAnchor()
        scheduleSleep()
    }

    private var anchor: android.media.MediaPlayer? = null

    /**
     * A silent, looping player. A wake lock keeps the CPU awake, but Chromium
     * still freezes a hidden page; holding a media session does not get frozen.
     * Inaudible on purpose - the point is the state, not the sound.
     */
    private fun startAnchor() {
        try {
            if (anchor != null) return
            anchor = android.media.MediaPlayer.create(this, R.raw.keepalive)?.apply {
                isLooping = true
                setVolume(0f, 0f)
                start()
            }
        } catch (_: Exception) { anchor = null }
    }

    private fun stopAnchor() {
        try { anchor?.stop() } catch (_: Exception) { }
        try { anchor?.release() } catch (_: Exception) { }
        anchor = null
    }

    private var sleepRunnable: Runnable? = null

    /**
     * Stop after the chosen number of minutes. Reading out loud is easy to start
     * and easy to forget; this is the switch that ends it.
     */
    private fun scheduleSleep() {
        cancelSleep()
        val mins = try {
            getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getInt("sleep_min", 0)
        } catch (_: Exception) { 0 }
        if (mins <= 0) return
        val r = Runnable { try { stopSelf() } catch (_: Exception) { } }
        sleepRunnable = r
        android.os.Handler(android.os.Looper.getMainLooper())
            .postDelayed(r, mins * 60_000L)
    }

    private fun cancelSleep() {
        sleepRunnable?.let {
            try { android.os.Handler(android.os.Looper.getMainLooper()).removeCallbacks(it) } catch (_: Exception) { }
        }
        sleepRunnable = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_TOGGLE -> try { TtsReader.toggle() } catch (_: Exception) { }
            ACTION_NEXT -> try { TtsReader.next() } catch (_: Exception) { }
            ACTION_PREV -> try { TtsReader.prev() } catch (_: Exception) { }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try { wake?.let { if (it.isHeld) it.release() } } catch (_: Exception) { }
        wake = null
        stopAnchor()
        cancelSleep()
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
        const val ACTION_TOGGLE = "com.vasudev.ekant.browser.TOGGLE_MEDIA"
        const val ACTION_NEXT = "com.vasudev.ekant.browser.NEXT_MEDIA"
        const val ACTION_PREV = "com.vasudev.ekant.browser.PREV_MEDIA"
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
