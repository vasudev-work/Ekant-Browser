package com.vasudev.ekant.browser

import android.content.SharedPreferences
import android.content.res.Configuration
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
    protected lateinit var rootColumn: LinearLayout
    protected abstract val titleRes: Int

    override fun onCreate(saved: Bundle?) {
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
        setTheme(ThemeHelper.res(this))
        super.onCreate(saved)

        val root = LinearLayout(this)
        rootColumn = root
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(attr(R.attr.bgColor))

        // Material's own toolbar: title styling, navigation icon and insets
        // come from the theme instead of being drawn by hand
        val bar = com.google.android.material.appbar.MaterialToolbar(this)
        bar.setBackgroundColor(attr(R.attr.barColor))
        bar.title = getString(titleRes)
        bar.setTitleTextColor(attr(R.attr.textPrimaryColor))
        bar.navigationIcon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_back)
        try { bar.setNavigationIconTint(attr(R.attr.textPrimaryColor)) } catch (_: Exception) { }
        bar.setNavigationOnClickListener { finish() }
        root.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))

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

    /** Insert a view just under the header bar (it survives rebuild()). */
    protected fun addTopView(v: android.view.View) { rootColumn.addView(v, 1) }

    /** Re-render the content (used after the extension list changes). */
    protected fun rebuild() {
        content.removeAllViews()
        build(SettingsBuilder(this, content), content)
    }

    protected fun attr(resid: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    protected fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()
}
