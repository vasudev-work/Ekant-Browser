package com.vasudev.ekant.browser

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Who made this, what they build, and a way to reach them. */
class DeveloperActivity : BaseSettingsActivity() {

    override val titleRes = R.string.dev_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.dev_title))
        b.note(getString(R.string.dev_about))

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.gravity = Gravity.CENTER_HORIZONTAL
        card.setPadding(dp(20), dp(22), dp(20), dp(18))
        card.background = cardBg()
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp(16), 0, dp(16), dp(8))
        card.layoutParams = lp

        // no photo to ship, so the mark stands in: initials on the brand gradient
        val avatar = TextView(this)
        avatar.text = "VD"
        avatar.setTextColor(Color.WHITE)
        avatar.textSize = 26f
        avatar.gravity = Gravity.CENTER
        val ring = GradientDrawable()
        ring.shape = GradientDrawable.OVAL
        ring.setColor(attr(R.attr.brandColor))
        avatar.background = ring
        val avLp = LinearLayout.LayoutParams(dp(84), dp(84))
        card.addView(avatar, avLp)

        val name = TextView(this)
        name.text = getString(R.string.dev_name)
        name.setTextColor(attr(R.attr.textPrimaryColor))
        name.textSize = 21f
        name.gravity = Gravity.CENTER
        name.setPadding(0, dp(14), 0, dp(2))
        card.addView(name)

        val handle = TextView(this)
        handle.text = getString(R.string.dev_handle)
        handle.setTextColor(attr(R.attr.brand2Color))
        handle.textSize = 14f
        handle.gravity = Gravity.CENTER
        card.addView(handle)

        val tag = TextView(this)
        tag.text = getString(R.string.dev_tagline)
        tag.setTextColor(attr(R.attr.textSecondaryColor))
        tag.textSize = 13f
        tag.gravity = Gravity.CENTER
        tag.setPadding(dp(6), dp(12), dp(6), dp(16))
        card.addView(tag)

        card.addView(stats())

        infoRow(card, getString(R.string.dev_location), getString(R.string.dev_location_value))
        infoRow(card, getString(R.string.dev_website), "github.com/vasudev-work")

        val btn = TextView(this)
        btn.text = getString(R.string.dev_github_btn)
        btn.setTextColor(Color.WHITE)
        btn.textSize = 15f
        btn.gravity = Gravity.CENTER
        btn.setPadding(0, dp(14), 0, dp(14))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(26).toFloat()
        bg.setColor(attr(R.attr.brandColor))
        btn.background = bg
        btn.isFocusable = false
        btn.setOnClickListener { open("https://github.com/vasudev-work") }
        val bLp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bLp.topMargin = dp(18)
        card.addView(btn, bLp)

        content.addView(card)

        val foot = TextView(this)
        foot.text = getString(R.string.dev_footer)
        foot.setTextColor(attr(R.attr.textSecondaryColor))
        foot.textSize = 12f
        foot.gravity = Gravity.CENTER
        foot.setPadding(0, dp(18), 0, dp(6))
        content.addView(foot)

        content.addView(indiaBar())
    }

    /** Three real numbers, not decoration. */
    private fun stats(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.setPadding(0, dp(4), 0, dp(4))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(16).toFloat()
        bg.setColor(attr(R.attr.surface2Color))
        row.background = bg
        row.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val exts = try { ExtensionStore(this).getAll().size } catch (_: Exception) { 0 }
        val rules = try { AdFilterEngine.ruleCount() } catch (_: Exception) { 0 }
        val screens = 34   // settings screens reachable from Settings

        cell(row, "" + exts, getString(R.string.r_extensions))
        cell(row, "" + screens, getString(R.string.dev_stat_screens))
        cell(row, compact(rules), getString(R.string.dev_stat_rules))
        return row
    }

    private fun cell(row: LinearLayout, value: String, label: String) {
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        col.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        col.setPadding(0, dp(12), 0, dp(12))
        val v = TextView(this)
        v.text = value
        v.setTextColor(attr(R.attr.textPrimaryColor))
        v.textSize = 19f
        v.gravity = Gravity.CENTER
        col.addView(v)
        val l = TextView(this)
        l.text = label
        l.setTextColor(attr(R.attr.textSecondaryColor))
        l.textSize = 11f
        l.gravity = Gravity.CENTER
        col.addView(l)
        row.addView(col)
    }

    private fun infoRow(card: LinearLayout, label: String, value: String) {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(0, dp(10), 0, dp(10))
        val l = TextView(this)
        l.text = label
        l.setTextColor(attr(R.attr.textSecondaryColor))
        l.textSize = 13f
        l.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        r.addView(l)
        val v = TextView(this)
        v.text = value
        v.setTextColor(attr(R.attr.textPrimaryColor))
        v.textSize = 13f
        v.gravity = Gravity.END
        r.addView(v)
        card.addView(r)
    }

    private fun indiaBar(): TextView {
        val tv = TextView(this)
        tv.text = getString(R.string.dev_make_in_india)
        tv.setTextColor(Color.WHITE)
        tv.textSize = 12f
        tv.gravity = Gravity.CENTER
        tv.setPadding(0, dp(10), 0, dp(10))
        val bg = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xFFFF9933.toInt(), 0xFFFF9933.toInt(), 0xFF138808.toInt()))
        bg.cornerRadius = dp(20).toFloat()
        tv.background = bg
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp(16), dp(4), dp(16), dp(16))
        tv.layoutParams = lp
        return tv
    }

    private fun cardBg(): GradientDrawable {
        val bg = GradientDrawable()
        bg.cornerRadius = dp(22).toFloat()
        bg.setColor(attr(R.attr.surfaceColor))
        bg.setStroke(dp(1), attr(R.attr.dividerColor))
        return bg
    }

    private fun compact(n: Int): String = when {
        n >= 1000000 -> (n / 1000000) .toString() + "M"
        n >= 1000 -> (n / 1000).toString() + "k"
        else -> "" + n
    }

    private fun open(url: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) { }
    }
}
