package com.vasudev.ekant.browser

import android.app.Application
import com.vasudev.Appstore.SettingsConfig

/**
 * Installs the reusable Vasu Settings module so its screens adopt Aekant's
 * identity and brand palette instead of the module defaults.
 */
class AekantApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val c = SettingsConfig()
        c.appName = "Aekant"
        c.developerName = "Vasudev Danganya"
        c.developerEmail = "Vasudev.work@rediffmail.com"
        c.github = "https://github.com/vasudev-work"
        c.upiId = "Vasudevupi@upi"
        c.upiPayee = "Vasudev"
        c.accent = 0xFFFF6600.toInt()
        c.darkBackground = 0xFF0B0E12.toInt()
        c.darkSurface = 0xFF161B21.toInt()
        c.lightBackground = 0xFFF4F6F8.toInt()
        c.lightSurface = 0xFFFFFFFF.toInt()
        c.cornerRadiusDp = 20
        SettingsConfig.install(c)

        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                java.io.File(filesDir, "crash.log")
                    .appendText("\n--- " + java.util.Date().toString() + " ---\n" +
                        android.util.Log.getStackTraceString(e) + "\n")
            } catch (_: Exception) { }
            prev?.uncaughtException(t, e)
        }
    }
}
