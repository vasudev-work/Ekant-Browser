package com.vasudev.ekant.browser

import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** History with search, date groups and time-range clearing. */
class HistoryActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_history

    private var query = ""
    private var added = false

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        if (!added) {
            added = true
            val et = EditText(this)
            et.hint = getString(R.string.hist_search)
            et.isSingleLine = true
            et.setTextColor(attr(R.attr.textPrimaryColor))
            et.setHintTextColor(attr(R.attr.textSecondaryColor))
            val bg = GradientDrawable()
            bg.cornerRadius = dp(20).toFloat()
            bg.setColor(attr(R.attr.surface2Color))
            et.background = bg
            et.setPadding(dp(14), dp(10), dp(14), dp(10))
            et.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { query = s?.toString() ?: ""; rebuild() }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
            })
            val box = LinearLayout(this)
            box.setPadding(dp(12), dp(8), dp(12), dp(2))
            box.addView(et)
            addTopView(box)
        }

        val all = parse()
        val filtered = if (query.isBlank()) all
        else all.filter { it.first.contains(query, true) || it.second.contains(query, true) }

        if (filtered.isEmpty()) {
            b.section(getString(R.string.s_history))
            b.note(getString(R.string.d_empty))
        } else {
            var lastDay = ""
            for (item in filtered) {
                val day = dayLabel(item.third)
                if (day != lastDay) {
                    lastDay = day
                    b.section(day)
                }
                val card = b.card()
                b.valueRow(card, if (item.first.isBlank()) item.second else item.first, item.second.take(40), null)
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) { delete(item.second, item.third) }
            }
        }

        b.section(getString(R.string.hist_clear))
        val c = b.card()
        b.navRow(c, getString(R.string.hist_hour), null) { clearSince(3600000L) }
        b.divider(c)
        b.navRow(c, getString(R.string.hist_day), null) { clearSince(86400000L) }
        b.divider(c)
        b.navRow(c, getString(R.string.hist_week), null) { clearSince(7 * 86400000L) }
        b.divider(c)
        b.navRow(c, getString(R.string.priv_clear_all), null) {
            prefs.edit().remove("history").apply(); rebuild()
        }
        b.note(getString(R.string.hist_note))
    }

    private fun parse(): List<Triple<String, String, Long>> {
        val out = ArrayList<Triple<String, String, Long>>()
        for (e in (prefs.getString("history", "") ?: "").split("\n")) {
            if (e.isBlank()) continue
            val p = e.split('\u0001')
            val t = p.getOrElse(0) { "" }
            val u = p.getOrElse(1) { "" }
            if (u.isBlank()) continue
            out.add(Triple(t, u, p.getOrElse(2) { "0" }.toLongOrNull() ?: 0L))
        }
        return out
    }

    private fun dayLabel(ts: Long): String {
        if (ts <= 0L) return getString(R.string.hist_older)
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = ts }
        val sameYear = now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
        if (sameYear && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
            return getString(R.string.hist_today)
        val y = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (sameYear && y.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR))
            return getString(R.string.hist_yesterday)
        return SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(ts))
    }

    private fun delete(url: String, ts: Long) {
        write(parse().filterNot { it.second == url && it.third == ts })
    }

    private fun clearSince(ms: Long) {
        val cutoff = System.currentTimeMillis() - ms
        write(parse().filter { it.third != 0L && it.third < cutoff })
    }

    private fun write(l: List<Triple<String, String, Long>>) {
        prefs.edit().putString("history", l.joinToString("\n") {
            it.first + '\u0001' + it.second + '\u0001' + it.third
        }).apply()
        rebuild()
    }
}
