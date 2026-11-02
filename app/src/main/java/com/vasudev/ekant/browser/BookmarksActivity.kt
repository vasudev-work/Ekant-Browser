package com.vasudev.ekant.browser

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast

/**
 * Bookmarks: a folder strip with counts, a search field, a sort menu, and rows
 * that carry the site's initial in a coloured tile - the shape people know from
 * other browsers, done without a network favicon lookup.
 */
class BookmarksActivity : BaseSettingsActivity() {

    override val titleRes = R.string.s_bookmarks

    private var query = ""
    private var searchAdded = false

    /** title \u0001 url, one per line, in the order they were saved. */
    private fun entries(): List<Pair<String, String>> =
        (prefs.getString("bookmarks", "") ?: "")
            .split("\n")
            .filter { it.contains('\u0001') }
            .map { it.substring(0, it.indexOf('\u0001')) to it.substring(it.indexOf('\u0001') + 1) }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val all = entries()
        if (!searchAdded) {
            searchAdded = true
            addSearchBox()
        }

        val shown = if (query.isBlank()) all
        else all.filter {
            it.first.contains(query, true) || it.second.contains(query, true)
        }

        if (query.isBlank()) folders(b, all)

        if (shown.isEmpty()) {
            if (query.isNotBlank()) b.note(getString(R.string.bm_no_hits))
            else b.emptyState(b.card(), R.drawable.ic_bookmark,
                getString(R.string.emp_bm_title), getString(R.string.emp_bm_sub),
                getString(R.string.emp_import),
                { startActivity(Intent(this, BackupActivity::class.java)) },
                getString(R.string.emp_browse), { finish() })
            return
        }

        if (query.isBlank()) {
            val card = b.card()
            b.navRow(card, getString(R.string.bm_sort) + ": " + sortName(), null) { pickSort() }
            b.divider(card)
            b.switchRow(card, getString(R.string.bm_visual), null,
                prefs.getBoolean("bm_visual", true)) { _, on ->
                prefs.edit().putBoolean("bm_visual", on).apply()
                rebuild()
            }
        }

        val card = b.card()
        val sorted = sortEntries(shown)
        sorted.forEachIndexed { i, (title, url) ->
            if (i > 0) b.divider(card)
            row(card, title, url)
        }
    }

    // ---------- pieces ----------

    private fun addSearchBox() {
        val box = LinearLayout(this)
        box.setPadding(dp(14), dp(10), dp(14), dp(2))
        val et = EditText(this)
        et.hint = getString(R.string.bm_search)
        et.isSingleLine = true
        et.setTextColor(attr(R.attr.textPrimaryColor))
        et.setHintTextColor(attr(R.attr.textSecondaryColor))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(22).toFloat()
        bg.setColor(attr(R.attr.surface2Color))
        et.background = bg
        et.setPadding(dp(16), dp(11), dp(16), dp(11))
        et.layoutParams = LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        et.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                query = s?.toString() ?: ""; rebuild()
            }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { }
        })
        box.addView(et)
        addTopView(box)
    }

    /** Two folders with their counts, the way a browser shows them. */
    private fun folders(b: SettingsBuilder, all: List<Pair<String, String>>) {
        val card = b.card()
        tile(card, R.drawable.ic_bookmark, getString(R.string.bm_mobile), all.size) { }
        b.divider(card)
        tile(card, R.drawable.ic_book, getString(R.string.rl_title), readingSize()) {
            try { startActivity(Intent(this, ReadingListActivity::class.java)) } catch (_: Exception) { }
        }
    }

    private fun readingSize(): Int = try {
        (prefs.getString("reading", "") ?: "").split("\n").count { it.contains('\u0001') }
    } catch (_: Exception) { 0 }

    private fun tile(card: LinearLayout, icon: Int, label: String, count: Int, on: () -> Unit) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(14), dp(12), dp(14), dp(12))
        row.isFocusable = false

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER
        val bg = GradientDrawable()
        bg.cornerRadius = dp(16).toFloat()
        bg.setColor(attr(R.attr.brandColor))
        box.background = bg
        val ic = android.widget.ImageView(this)
        ic.setImageResource(icon)
        ic.setColorFilter(0xFFFFFFFF.toInt())
        box.addView(ic, LinearLayout.LayoutParams(dp(24), dp(24)))
        val n = TextView(this)
        n.text = "" + count
        n.setTextColor(0xFFFFFFFF.toInt())
        n.textSize = 13f
        n.gravity = Gravity.CENTER
        n.setTypeface(null, android.graphics.Typeface.BOLD)
        box.addView(n)
        row.addView(box, LinearLayout.LayoutParams(dp(64), dp(64)))

        val t = TextView(this)
        t.text = label
        t.setTextColor(attr(R.attr.textPrimaryColor))
        t.textSize = 16f
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp.leftMargin = dp(16)
        row.addView(t, lp)

        row.setOnClickListener { on() }
        card.addView(row)
    }

    private fun sortEntries(list: List<Pair<String, String>>): List<Pair<String, String>> =
        when (prefs.getString("bm_sort", "manual")) {
            "newest" -> list.reversed()
            "az" -> list.sortedBy { it.first.lowercase() }
            "za" -> list.sortedByDescending { it.first.lowercase() }
            else -> list
        }

    private fun sortName(): String = when (prefs.getString("bm_sort", "manual")) {
        "newest" -> getString(R.string.bm_sort_newest)
        "az" -> getString(R.string.bm_sort_az)
        "za" -> getString(R.string.bm_sort_za)
        else -> getString(R.string.bm_sort_manual)
    }

    private fun pickSort() {
        val names = arrayOf(
            getString(R.string.bm_sort_manual), getString(R.string.bm_sort_newest),
            getString(R.string.bm_sort_az), getString(R.string.bm_sort_za))
        val keys = arrayOf("manual", "newest", "az", "za")
        val current = keys.indexOf(prefs.getString("bm_sort", "manual"))
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.bm_sort))
            .setSingleChoiceItems(names, current) { d, which ->
                prefs.edit().putString("bm_sort", keys[which]).apply()
                d.dismiss()
                rebuild()
            }
            .show()
    }

    private fun row(card: LinearLayout, title: String, url: String) {
        val host = try {
            Uri.parse(url).host?.removePrefix("www.") ?: url
        } catch (_: Exception) { url }
        val label = if (title.isBlank()) host else title
        val visual = prefs.getBoolean("bm_visual", true)

        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(12), dp(10), dp(4), dp(10))
        r.isFocusable = false

        val side = if (visual) 52 else 34
        val mark = TextView(this)
        mark.text = (label.firstOrNull() ?: '?').uppercaseChar().toString()
        mark.setTextColor(0xFFFFFFFF.toInt())
        mark.textSize = if (visual) 18f else 14f
        mark.gravity = Gravity.CENTER
        mark.setTypeface(null, android.graphics.Typeface.BOLD)
        val bg = GradientDrawable()
        bg.cornerRadius = dp(if (visual) 14 else 10).toFloat()
        bg.setColor(avatarColour(host))
        mark.background = bg
        r.addView(mark, LinearLayout.LayoutParams(dp(side), dp(side)))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val clp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        clp.leftMargin = dp(14)
        col.layoutParams = clp
        val t = TextView(this)
        t.text = label
        t.setTextColor(attr(R.attr.textPrimaryColor))
        t.textSize = 15f
        t.maxLines = 1
        t.ellipsize = android.text.TextUtils.TruncateAt.END
        col.addView(t)
        val s = TextView(this)
        s.text = host
        s.setTextColor(attr(R.attr.textSecondaryColor))
        s.textSize = 12f
        s.maxLines = 1
        col.addView(s)
        r.addView(col)

        val more = TextView(this)
        more.text = "\u22ee"
        more.textSize = 20f
        more.gravity = Gravity.CENTER
        more.setTextColor(attr(R.attr.textSecondaryColor))
        more.setPadding(dp(10), 0, dp(10), 0)
        more.isFocusable = false
        more.setOnClickListener { menu(more, title, url) }
        r.addView(more)

        r.setOnClickListener { open(url) }
        card.addView(r)
    }

    private fun menu(anchor: TextView, title: String, url: String) {
        val pm = PopupMenu(this, anchor)
        pm.menu.add(0, 1, 0, getString(R.string.dl_open))
        pm.menu.add(0, 2, 1, getString(R.string.bm_copy))
        pm.menu.add(0, 3, 2, getString(R.string.ext_delete))
        pm.setOnMenuItemClickListener { m ->
            when (m.itemId) {
                1 -> open(url)
                2 -> copy(url)
                3 -> { remove(title, url) }
            }
            true
        }
        pm.show()
    }

    private fun open(url: String) {
        try {
            val i = Intent(this, MainActivity::class.java)
                .putExtra("open_url", url)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        } catch (_: Exception) { }
    }

    private fun copy(url: String) {
        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("url", url))
            Toast.makeText(this, getString(R.string.bm_copied), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) { }
    }

    private fun remove(title: String, url: String) {
        val line = title + '\u0001' + url
        val left = (prefs.getString("bookmarks", "") ?: "")
            .split("\n").filter { it.isNotBlank() && it != line }
        prefs.edit().putString("bookmarks", left.joinToString("\n")).apply()
        rebuild()
    }

    /** A stable colour per host, so the same site always looks the same. */
    private fun avatarColour(host: String): Int {
        val palette = intArrayOf(
            0xFF1E88E5.toInt(), 0xFF8E24AA.toInt(), 0xFF00897B.toInt(),
            0xFFE53935.toInt(), 0xFFF4511E.toInt(), 0xFF3949AB.toInt(),
            0xFF43A047.toInt(), 0xFF6D4C41.toInt())
        var x = 0
        for (c in host) x = (x * 31 + c.code) % palette.size
        return palette[x]
    }
}
