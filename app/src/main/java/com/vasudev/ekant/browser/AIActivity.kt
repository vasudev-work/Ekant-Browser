package com.vasudev.ekant.browser

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Native AI chat that talks to any OpenAI-compatible endpoint (ported from My Browser). */
class AIActivity : AppCompatActivity() {

    private lateinit var log: LinearLayout
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private val prefs by lazy { getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE) }

    override fun onCreate(saved: Bundle?) {
        val dark = prefs.getBoolean("dark", true)
        setTheme(if (dark) R.style.Theme_Aekant else R.style.Theme_Aekant_Light)
        super.onCreate(saved)
        setContentView(R.layout.activity_ai)

        log = findViewById(R.id.ai_log)
        scroll = findViewById(R.id.ai_scroll)
        input = findViewById(R.id.ai_input)

        findViewById<ImageButton>(R.id.ai_back).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.ai_settings).setOnClickListener { configDialog() }
        findViewById<ImageButton>(R.id.ai_send).setOnClickListener { send() }

        if (endpoint().isEmpty()) bubble(getString(R.string.ai_setup), false)
        else bubble(getString(R.string.ai_hello), false)
    }

    private fun endpoint() = prefs.getString("ai_endpoint", "")!!.trim()
    private fun apiKey() = prefs.getString("ai_key", "")!!.trim()
    private fun model() = prefs.getString("ai_model", "gpt-4o-mini")!!.trim()

    private fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    private fun bubble(text: String, user: Boolean) {
        val t = TextView(this)
        t.text = text
        t.textSize = 14f
        t.setTextColor(if (user) 0xFF0B0E12.toInt() else attr(R.attr.textPrimaryColor))
        t.setBackgroundResource(if (user) R.drawable.bubble_user else R.drawable.bubble_bot)
        val p = dp(12)
        t.setPadding(p, p, p, p)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(8)
        lp.gravity = if (user) Gravity.END else Gravity.START
        lp.leftMargin = if (user) dp(40) else 0
        lp.rightMargin = if (user) 0 else dp(40)
        t.layoutParams = lp
        log.addView(t)
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun send() {
        val q = input.text.toString().trim()
        if (q.isEmpty()) return
        input.setText("")
        bubble(q, true)

        if (endpoint().isEmpty()) { bubble(getString(R.string.ai_setup), false); return }

        val thinking = TextView(this)
        thinking.text = getString(R.string.ai_thinking)
        thinking.textSize = 13f
        thinking.setTextColor(attr(R.attr.textSecondaryColor))
        thinking.setPadding(dp(6), dp(10), 0, 0)
        log.addView(thinking)

        Thread {
            val reply = callApi(q)
            runOnUiThread {
                log.removeView(thinking)
                bubble(reply, false)
            }
        }.start()
    }

    private fun callApi(prompt: String): String {
        var c: HttpURLConnection? = null
        try {
            val url = URL(endpoint())
            c = url.openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.connectTimeout = 15000
            c.readTimeout = 30000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            if (apiKey().isNotEmpty()) c.setRequestProperty("Authorization", "Bearer " + apiKey())

            val body = JSONObject()
            body.put("model", model())
            val msgs = JSONArray()
            val m = JSONObject()
            m.put("role", "user")
            m.put("content", prompt)
            msgs.put(m)
            body.put("messages", msgs)

            val os: OutputStream = c.outputStream
            os.write(body.toString().toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            val code = c.responseCode
            val r = BufferedReader(InputStreamReader(
                if (code >= 400) c.errorStream else c.inputStream, "UTF-8"))
            val sb = StringBuilder()
            var line = r.readLine()
            while (line != null) { sb.append(line); line = r.readLine() }
            r.close()

            if (code >= 400) return getString(R.string.ai_error)
            val json = JSONObject(sb.toString())
            if (json.has("choices")) {
                return json.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content")
            }
            if (json.has("reply")) return json.getString("reply")
            if (json.has("response")) return json.getString("response")
            return sb.toString()
        } catch (_: Exception) {
            return getString(R.string.ai_error)
        } finally {
            c?.disconnect()
        }
    }

    private fun configDialog() {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val p = dp(20)
        box.setPadding(p, dp(8), p, 0)

        val e1 = EditText(this)
        e1.hint = getString(R.string.ai_endpoint)
        e1.setText(prefs.getString("ai_endpoint", ""))
        box.addView(e1)

        val e2 = EditText(this)
        e2.hint = getString(R.string.ai_key)
        e2.setText(prefs.getString("ai_key", ""))
        box.addView(e2)

        val e3 = EditText(this)
        e3.hint = getString(R.string.ai_model)
        e3.setText(prefs.getString("ai_model", "gpt-4o-mini"))
        box.addView(e3)

        AlertDialog.Builder(this).setTitle(R.string.ai_config).setView(box)
            .setPositiveButton(R.string.ai_save) { _, _ ->
                prefs.edit()
                    .putString("ai_endpoint", e1.text.toString().trim())
                    .putString("ai_key", e2.text.toString().trim())
                    .putString("ai_model", e3.text.toString().trim())
                    .apply()
                bubble("AI settings saved.", false)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
