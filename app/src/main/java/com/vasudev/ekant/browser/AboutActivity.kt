package com.vasudev.ekant.browser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(saved: Bundle?) {
        setTheme(ThemeHelper.res(this))
        super.onCreate(saved)
        setContentView(R.layout.activity_about)

        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.upi).setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("UPI", getString(R.string.upi_id)))
            Toast.makeText(this, "UPI ID copied", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.github).setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vasudev-work"))
            try { startActivity(i) } catch (_: Exception) { }
        }
        findViewById<View>(R.id.feedback).setOnClickListener {
            val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.feedback_email)))
            try { startActivity(i) }
            catch (_: Exception) {
                Toast.makeText(this, getString(R.string.feedback_email), Toast.LENGTH_LONG).show()
            }
        }
    }
}
