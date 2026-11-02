package com.vasudev.ekant.browser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * A UPI payment page. The QR is drawn from the deep link at runtime by the
 * ZXing encoder the app already carries for scanning, so there is no image to
 * ship and no third-party payment SDK.
 */
class DonateActivity : BaseSettingsActivity() {

    override val titleRes = R.string.don_title

    private val upiId = "Vasudevupi@upi"
    private val payee = "Vasudev"

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.don_title))
        b.note(getString(R.string.don_intro))

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(16), dp(16), dp(16), dp(16))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(20).toFloat()
        bg.setColor(attr(R.attr.surfaceColor))
        bg.setStroke(dp(1), attr(R.attr.dividerColor))
        card.background = bg
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp(16), 0, dp(16), dp(10))
        card.layoutParams = lp

        val head = TextView(this)
        head.text = getString(R.string.don_support)
        head.setTextColor(attr(R.attr.brand2Color))
        head.textSize = 12f
        head.letterSpacing = 0.06f
        card.addView(head)

        val body = TextView(this)
        body.text = getString(R.string.don_body)
        body.setTextColor(attr(R.attr.textSecondaryColor))
        body.textSize = 13f
        body.setPadding(0, dp(8), 0, dp(14))
        card.addView(body)

        // the QR sits on white, whatever the theme is - a scanner needs contrast
        val qrBox = LinearLayout(this)
        qrBox.orientation = LinearLayout.VERTICAL
        qrBox.gravity = Gravity.CENTER
        qrBox.setPadding(dp(12), dp(14), dp(12), dp(14))
        val qbg = GradientDrawable()
        qbg.cornerRadius = dp(16).toFloat()
        qbg.setColor(attr(R.attr.surface2Color))
        qrBox.background = qbg
        val qTitle = TextView(this)
        qTitle.text = getString(R.string.don_scan)
        qTitle.setTextColor(attr(R.attr.textPrimaryColor))
        qTitle.textSize = 14f
        qTitle.gravity = Gravity.CENTER
        qrBox.addView(qTitle)
        val qr = qrBitmap()
        if (qr != null) {
            val img = ImageView(this)
            img.setImageBitmap(qr)
            img.setBackgroundColor(Color.WHITE)
            img.setPadding(dp(10), dp(10), dp(10), dp(10))
            val ilp = LinearLayout.LayoutParams(dp(212), dp(212))
            ilp.topMargin = dp(12)
            qrBox.addView(img, ilp)
        } else {
            val err = TextView(this)
            err.text = getString(R.string.don_qr_failed)
            err.setTextColor(attr(R.attr.textSecondaryColor))
            err.textSize = 12f
            err.gravity = Gravity.CENTER
            err.setPadding(0, dp(12), 0, dp(6))
            qrBox.addView(err)
        }
        card.addView(qrBox)

        card.addView(upiRow())
        card.addView(payRow())

        val thanks = TextView(this)
        thanks.text = getString(R.string.don_thanks)
        thanks.setTextColor(attr(R.attr.textSecondaryColor))
        thanks.textSize = 13f
        thanks.gravity = Gravity.CENTER
        thanks.setPadding(0, dp(14), 0, dp(2))
        card.addView(thanks)

        content.addView(card)

        val foot = TextView(this)
        foot.text = getString(R.string.dev_footer)
        foot.setTextColor(attr(R.attr.textSecondaryColor))
        foot.textSize = 12f
        foot.gravity = Gravity.CENTER
        foot.setPadding(0, dp(14), 0, dp(6))
        content.addView(foot)
        content.addView(indiaBar())
    }

    private fun upiRow(): LinearLayout {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(14), dp(12), dp(8), dp(12))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(14).toFloat()
        bg.setColor(attr(R.attr.surface2Color))
        r.background = bg
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(10)
        r.layoutParams = lp

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        val t = TextView(this)
        t.text = getString(R.string.don_upi)
        t.setTextColor(attr(R.attr.textSecondaryColor))
        t.textSize = 11f
        col.addView(t)
        val v = TextView(this)
        v.text = upiId
        v.setTextColor(attr(R.attr.textPrimaryColor))
        v.textSize = 14f
        col.addView(v)
        r.addView(col)

        val copy = TextView(this)
        copy.text = getString(R.string.don_copy)
        copy.setTextColor(attr(R.attr.textPrimaryColor))
        copy.textSize = 13f
        copy.setPadding(dp(14), dp(7), dp(14), dp(7))
        val cbg = GradientDrawable()
        cbg.cornerRadius = dp(16).toFloat()
        cbg.setColor(attr(R.attr.surfaceColor))
        copy.background = cbg
        copy.isFocusable = false
        copy.setOnClickListener {
            try {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("UPI", upiId))
                Toast.makeText(this, getString(R.string.don_copied), Toast.LENGTH_SHORT).show()
            } catch (_: Exception) { }
        }
        r.addView(copy)
        return r
    }

    private fun payRow(): LinearLayout {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.VERTICAL
        r.setPadding(dp(14), dp(12), dp(14), dp(12))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(14).toFloat()
        bg.setColor(attr(R.attr.surface2Color))
        r.background = bg
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(10)
        r.layoutParams = lp
        r.isFocusable = false

        val t = TextView(this)
        t.text = getString(R.string.don_pay)
        t.setTextColor(attr(R.attr.textPrimaryColor))
        t.textSize = 14f
        r.addView(t)
        val s = TextView(this)
        s.text = if (upiApps()) getString(R.string.don_pay_sub)
               else getString(R.string.don_no_app)
        s.setTextColor(attr(R.attr.textSecondaryColor))
        s.textSize = 12f
        r.addView(s)
        r.setOnClickListener { pay() }
        return r
    }

    private fun pay() {
        try {
            val u = "upi://pay?pa=" + upiId + "&pn=" + Uri.encode(payee) + "&cu=INR"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)))
        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.don_no_app), Toast.LENGTH_LONG).show()
        }
    }

    private fun upiApps(): Boolean = try {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("upi://pay?pa=" + upiId + "&cu=INR"))
        packageManager.queryIntentActivities(i, 0).isNotEmpty()
    } catch (_: Exception) { false }

    /** ZXing already ships for the scanner; it encodes just as well. */
    private fun qrBitmap(): Bitmap? = try {
        val text = "upi://pay?pa=" + upiId + "&pn=" + Uri.encode(payee) + "&cu=INR"
        val size = 512
        val bits = com.google.zxing.qrcode.QRCodeWriter().encode(
            text, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (y in 0 until size) for (x in 0 until size) {
            bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
        }
        bmp
    } catch (_: Exception) { null }

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
}
