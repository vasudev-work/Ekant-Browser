package com.vasudev.ekant.browser

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/** A colour laid over every page, to take the glare off. */
class ScreenFilterActivity : BaseSettingsActivity() {

    override val titleRes = R.string.sf_title

    private val swatches = listOf(
        "#FF9800" to "Amber", "#FFD54F" to "Warm", "#81C784" to "Green",
        "#64B5F6" to "Blue", "#BA68C8" to "Violet", "#000000" to "Dim"
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.sf_title))
        val c = b.card()
        b.switchRow(c, getString(R.string.sf_on), getString(R.string.sf_on_sum),
            prefs.getBoolean("screen_filter", false)) { _, on ->
            prefs.edit().putBoolean("screen_filter", on).apply(); rebuild()
        }
        b.divider(c)
        b.switchRow(c, getString(R.string.sf_night), getString(R.string.sf_night_sum),
            prefs.getBoolean("screen_filter_night", false)) { _, on ->
            prefs.edit().putBoolean("screen_filter_night", on).apply()
        }

        b.section(getString(R.string.sf_colour))
        val cc = b.card()
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(dp(16), dp(14), dp(16), dp(14))
        row.gravity = Gravity.CENTER_VERTICAL
        val current = prefs.getString("screen_filter_color", "#FF9800") ?: "#FF9800"
        for ((hex, label) in swatches) {
            val sw = TextView(this)
            sw.text = ""
            val d = GradientDrawable()
            d.shape = GradientDrawable.OVAL
            d.setColor(try { Color.parseColor(hex) } catch (_: Exception) { Color.GRAY })
            d.setStroke(if (hex.equals(current, true)) dp(3) else dp(1),
                if (hex.equals(current, true)) attr(R.attr.brandColor) else attr(R.attr.dividerColor))
            sw.background = d
            val lp = LinearLayout.LayoutParams(dp(38), dp(38))
            lp.rightMargin = dp(12)
            sw.layoutParams = lp
            sw.setOnClickListener {
                prefs.edit().putString("screen_filter_color", hex).apply(); rebuild()
            }
            row.addView(sw)
        }
        cc.addView(row)

        b.section(getString(R.string.sf_strength))
        val cs = b.card()
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(16), dp(12), dp(16), dp(14))
        val label = TextView(this)
        label.setTextColor(attr(R.attr.textSecondaryColor))
        label.textSize = 12f
        box.addView(label)
        val bar = SeekBar(this)
        bar.max = 120
        bar.progress = prefs.getInt("screen_filter_alpha", 40)
        label.text = getString(R.string.sf_strength) + ": " + bar.progress + "%"
        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                label.text = getString(R.string.sf_strength) + ": " + p + "%"
                if (fromUser) prefs.edit().putInt("screen_filter_alpha", p).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) { }
            override fun onStopTrackingTouch(sb: SeekBar?) { }
        })
        box.addView(bar)
        cs.addView(box)
        b.note(getString(R.string.sf_note))
    }
}
