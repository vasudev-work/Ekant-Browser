package com.vasudev.ekant.browser

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial

/** Card/row builder shared by the settings screens and the menu (ported from My Browser). */
class SettingsBuilder(private val a: Context, private val root: LinearLayout) {

    private val textColor: Int = attr(android.R.attr.textColorPrimary)
    private val mutedColor: Int = attr(android.R.attr.textColorSecondary)
    private val accentColor: Int = a.resources.getColor(R.color.brand_orange)

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        a.theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    private fun dp(v: Int) = (a.resources.displayMetrics.density * v).toInt()

    fun section(title: String) {
        val t = TextView(a)
        t.text = title
        t.setTextColor(accentColor)
        t.textSize = 12f
        t.letterSpacing = 0.06f
        t.setPadding(dp(22), dp(20), dp(8), dp(8))
        root.addView(t)
    }

    fun card(): LinearLayout {
        val c = LinearLayout(a)
        c.orientation = LinearLayout.VERTICAL
        c.setBackgroundResource(R.drawable.card_bg)
        c.elevation = dp(2).toFloat()
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = dp(16)
        lp.rightMargin = dp(16)
        c.layoutParams = lp
        root.addView(c)
        return c
    }

    fun note(text: String) {
        val t = TextView(a)
        t.text = text
        t.setTextColor(mutedColor)
        t.textSize = 12f
        t.setPadding(dp(26), dp(10), dp(26), dp(0))
        root.addView(t)
    }

    private fun rowBase(card: LinearLayout): LinearLayout {
        val r = LinearLayout(a)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(16), dp(15), dp(16), dp(15))
        card.addView(r)
        return r
    }

    private fun textCol(row: LinearLayout, title: String, summary: String?, color: Int) {
        val col = LinearLayout(a)
        col.orientation = LinearLayout.VERTICAL
        col.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        val t = TextView(a)
        t.text = title
        t.setTextColor(color)
        t.textSize = 15f
        col.addView(t)
        if (!summary.isNullOrEmpty()) {
            val s = TextView(a)
            s.text = summary
            s.setTextColor(mutedColor)
            s.textSize = 12f
            s.setPadding(0, dp(3), 0, 0)
            col.addView(s)
        }
        row.addView(col)
    }

    private fun addIcon(row: LinearLayout, iconRes: Int) {
        val ic = ImageView(a)
        ic.setImageResource(iconRes)
        ic.setColorFilter(mutedColor)
        val ilp = LinearLayout.LayoutParams(dp(22), dp(22))
        ilp.rightMargin = dp(16)
        ic.layoutParams = ilp
        row.addView(ic)
    }

    private fun addChevron(row: LinearLayout) {
        val chev = ImageView(a)
        chev.setImageResource(R.drawable.ic_chev)
        chev.setColorFilter(mutedColor)
        chev.layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
        row.addView(chev)
    }

    fun divider(card: LinearLayout) {
        val v = View(a)
        v.setBackgroundColor(0x1A808080)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        lp.leftMargin = dp(16)
        lp.rightMargin = dp(16)
        v.layoutParams = lp
        card.addView(v)
    }

    fun navRow(card: LinearLayout, title: String, summary: String?, l: View.OnClickListener?) {
        val r = rowBase(card)
        textCol(r, title, summary, textColor)
        addChevron(r)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun iconRow(card: LinearLayout, iconRes: Int, title: String, summary: String?, l: View.OnClickListener?) {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, summary, textColor)
        addChevron(r)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun iconValueRow(card: LinearLayout, iconRes: Int, title: String, value: String, l: View.OnClickListener?) {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, null, textColor)
        val v = TextView(a)
        v.text = value
        v.setTextColor(accentColor)
        v.textSize = 13f
        r.addView(v)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun disabledRow(card: LinearLayout, title: String, summary: String?) {
        val r = rowBase(card)
        textCol(r, title, summary, mutedColor)
        val tag = TextView(a)
        tag.text = "ENGINE"
        tag.textSize = 10f
        tag.letterSpacing = 0.08f
        tag.setTextColor(mutedColor)
        tag.setBackgroundResource(R.drawable.tag_bg)
        tag.setPadding(dp(8), dp(3), dp(8), dp(3))
        r.addView(tag)
        r.isEnabled = false
        r.alpha = 0.6f
    }

    fun switchRow(card: LinearLayout, title: String, summary: String?, checked: Boolean,
                  l: CompoundButton.OnCheckedChangeListener?): SwitchMaterial {
        val r = rowBase(card)
        textCol(r, title, summary, textColor)
        val sw = SwitchMaterial(a)
        sw.isChecked = checked
        if (l != null) sw.setOnCheckedChangeListener(l)
        r.addView(sw)
        return sw
    }

    fun iconSwitchRow(card: LinearLayout, iconRes: Int, title: String, summary: String?,
                      checked: Boolean, l: CompoundButton.OnCheckedChangeListener?): SwitchMaterial {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, summary, textColor)
        val sw = SwitchMaterial(a)
        sw.isChecked = checked
        if (l != null) sw.setOnCheckedChangeListener(l)
        r.addView(sw)
        return sw
    }

    fun radioRow(card: LinearLayout, title: String, summary: String?, checked: Boolean, l: View.OnClickListener?) {
        val r = rowBase(card)
        val dot = ImageView(a)
        dot.setImageResource(if (checked) R.drawable.radio_on else R.drawable.radio_off)
        val dlp = LinearLayout.LayoutParams(dp(22), dp(22))
        dlp.rightMargin = dp(14)
        dot.layoutParams = dlp
        r.addView(dot)
        textCol(r, title, summary, textColor)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun valueRow(card: LinearLayout, title: String, value: String, l: View.OnClickListener?) {
        val r = rowBase(card)
        textCol(r, title, null, textColor)
        val v = TextView(a)
        v.text = value
        v.setTextColor(accentColor)
        v.textSize = 13f
        r.addView(v)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }
}
