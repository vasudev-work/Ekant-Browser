package com.vasudev.ekant.browser

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Scaffold for the settings sub-screens: back header + scrollable content. */
abstract class BaseSettingsActivity : AppCompatActivity() {

    protected lateinit var prefs: SharedPreferences
    protected lateinit var content: LinearLayout
    protected abstract val titleRes: Int

    override fun onCreate(saved: Bundle?) {
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        val dark = prefs.getBoolean("dark", true)
        setTheme(if (dark) R.style.Theme_Aekant else R.style.Theme_Aekant_Light)
        super.onCreate(saved)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(attr(android.R.attr.colorBackground))

        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        header.setBackgroundColor(attr(R.attr.barColor))
        header.setPadding(dp(6), dp(6), dp(6), dp(6))

        val back = ImageView(this)
        back.setImageResource(R.drawable.ic_back)
        back.setColorFilter(attr(android.R.attr.textColorPrimary))
        back.setOnClickListener { finish() }
        header.addView(back, LinearLayout.LayoutParams(dp(42), dp(42)))

        val title = TextView(this)
        title.text = getString(titleRes)
        title.setTextColor(attr(android.R.attr.textColorPrimary))
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(dp(8), 0, 0, 0)
        header.addView(title)

        root.addView(header, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val scroll = ScrollView(this)
        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(0, 0, 0, dp(24))
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        build(SettingsBuilder(this, content), content)
    }

    protected abstract fun build(b: SettingsBuilder, content: LinearLayout)

    protected fun attr(resid: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    protected fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()
}
