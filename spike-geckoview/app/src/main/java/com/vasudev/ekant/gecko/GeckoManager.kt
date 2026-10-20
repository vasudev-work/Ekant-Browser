package com.vasudev.ekant.gecko

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

/** One shared GeckoRuntime for the whole app (Mozilla Gecko engine). */
object GeckoManager {

    @Volatile
    private var runtime: GeckoRuntime? = null

    fun get(context: Context): GeckoRuntime {
        runtime?.let { return it }
        synchronized(this) {
            runtime?.let { return it }
            val settings = GeckoRuntimeSettings.Builder().build()
            val created = GeckoRuntime.create(context.applicationContext, settings)
            runtime = created
            return created
        }
    }
}
