package com.vasudev.ekant.browser

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/** Reusable animated bottom sheet (ported from My Browser). */
class Sheet private constructor(private val a: AppCompatActivity) {

    fun interface OnItemClick { fun onItem(index: Int) }

    private val root: View = LayoutInflater.from(a).inflate(R.layout.sheet, null)
    private val scrim: View = root.findViewById(R.id.sheet_scrim)
    private val panel: View = root.findViewById(R.id.sheet_panel)
    private val title: TextView = root.findViewById(R.id.sheet_title)
    private val items: LinearLayout = root.findViewById(R.id.sheet_items)
    private val buttons: LinearLayout = root.findViewById(R.id.sheet_buttons)

    init {
        val dark = a.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
            .getBoolean("dark", true)
        panel.setBackgroundResource(if (dark) R.drawable.panel_bg_dark else R.drawable.panel_bg_light)
        scrim.setOnClickListener { dismiss() }
    }

    private fun dp(v: Int) = (a.resources.displayMetrics.density * v).toInt()

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        a.theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    fun item(label: String, index: Int, cb: OnItemClick?): Sheet {
        val t = TextView(a)
        t.text = label
        t.setTextColor(attr(R.attr.textPrimaryColor))
        t.textSize = 15f
        t.setPadding(dp(20), dp(15), dp(20), dp(15))
        t.setOnClickListener { dismiss(); cb?.onItem(index) }
        items.addView(t)
        val div = View(a)
        div.setBackgroundColor(attr(R.attr.dividerColor))
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        lp.leftMargin = dp(20)
        lp.rightMargin = dp(20)
        div.layoutParams = lp
        items.addView(div)
        return this
    }

    fun content(v: View): Sheet { items.addView(v); return this }

    fun action(label: String, r: Runnable?): Sheet {
        val t = TextView(a)
        t.text = label
        t.setTextColor(attr(R.attr.brandColor))
        t.textSize = 14f
        t.setTypeface(null, android.graphics.Typeface.BOLD)
        t.setPadding(dp(22), dp(16), dp(22), dp(16))
        t.setOnClickListener { dismiss(); r?.run() }
        buttons.addView(t)
        buttons.visibility = View.VISIBLE
        return this
    }

    fun show() {
        val decor = a.findViewById<ViewGroup>(android.R.id.content)
        decor.addView(root)
        val h = (a.resources.displayMetrics.heightPixels * 0.62).toInt()
        panel.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM)
        scrim.alpha = 0f
        scrim.animate().alpha(1f).setDuration(180).start()
        panel.translationY = h.toFloat()
        panel.animate().translationY(0f).setDuration(200).start()
    }

    fun dismiss() {
        panel.animate().translationY(panel.height.toFloat()).setDuration(160)
            .withEndAction {
                val decor = a.findViewById<ViewGroup>(android.R.id.content)
                decor.removeView(root)
            }.start()
        scrim.animate().alpha(0f).setDuration(160).start()
    }

    companion object {
        fun build(a: AppCompatActivity, titleText: String): Sheet {
            val s = Sheet(a)
            s.title.text = titleText
            return s
        }
    }
}
