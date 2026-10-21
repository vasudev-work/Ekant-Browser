package com.vasudev.ekant.browser

import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

/** A small ring buffer of recent requests, for the network log screen. */
object NetLog {

    @Volatile var enabled: Boolean = false

    private val items = Collections.synchronizedList(ArrayList<String>())
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun add(url: String, blocked: Boolean) {
        if (!enabled) return
        val line = fmt.format(Date()) + "  " + (if (blocked) "BLOCK " else "allow ") + url
        synchronized(items) {
            items.add(line)
            while (items.size > 400) items.removeAt(0)
        }
    }

    fun entries(): List<String> = synchronized(items) { ArrayList(items.reversed()) }

    fun clear() = synchronized(items) { items.clear() }

    fun size(): Int = items.size
}
