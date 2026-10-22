package com.vasudev.ekant.browser

import android.content.Context
import android.content.res.Configuration

/** Picks one of the four themes: Aekant / Quiet Nature, each dark or light. */
object ThemeHelper {

    fun prefs(context: Context) =
        context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    fun isDark(context: Context): Boolean =
        when (prefs(context).getString("theme_mode", "dark")) {
            "light" -> false
            "system" -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
            else -> true
        }

    fun isQuiet(context: Context): Boolean =
        (prefs(context).getString("theme_brand", "aekant") ?: "aekant") == "quiet"

    fun isAmoled(context: Context): Boolean =
        prefs(context).getBoolean("amoled_ui", false)

    fun res(context: Context): Int {
        val dark = isDark(context)
        val quiet = isQuiet(context)
        val amoled = isAmoled(context) && dark
        return when {
            amoled && quiet -> R.style.Theme_Aekant_QuietAmoled
            amoled -> R.style.Theme_Aekant_Amoled
            quiet && dark -> R.style.Theme_Aekant_QuietDark
            quiet -> R.style.Theme_Aekant_QuietLight
            dark -> R.style.Theme_Aekant
            else -> R.style.Theme_Aekant_Light
        }
    }
}
