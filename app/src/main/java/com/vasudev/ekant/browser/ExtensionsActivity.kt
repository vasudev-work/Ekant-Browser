package com.vasudev.ekant.browser

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

/** Extensions, laid out like a browser add-ons list: icon, name, one-line note, switch. */
class ExtensionsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_extensions

    private val store by lazy { ExtensionStore(this) }

    private val palette = intArrayOf(
        0xFF3B82F6.toInt(), 0xFF8CC63F.toInt(), 0xFFFF6600.toInt(),
        0xFFA855F7.toInt(), 0xFF14B8A6.toInt(), 0xFFEF4444.toInt()
    )

    private val importFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) importFrom(uri)
        }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val installed = store.getAll()
        val haveIds = installed.map { it.id }.toHashSet()

        b.section(getString(R.string.ext_installed))
        if (installed.isEmpty()) b.note(getString(R.string.ext_none))
        else installed.forEach { addCard(content, it, false) }

        val catalog = store.catalog().filter { !haveIds.contains(it.id) }
        if (catalog.isNotEmpty()) {
            b.section(getString(R.string.ext_recommended))
            catalog.forEach { addCard(content, it, true) }
        }

        b.section(getString(R.string.ext_add))
        val add = b.card()
        b.iconRow(add, R.drawable.ic_new_tab, getString(R.string.ext_add_manual),
            getString(R.string.ext_add_manual_sum)) { addDialog() }
        b.divider(add)
        b.iconRow(add, R.drawable.ic_folder, getString(R.string.ext_upload),
            getString(R.string.ext_upload_sum)) { importFile.launch(arrayOf("*/*")) }

        b.note(getString(R.string.ext_note))
        b.note(getString(R.string.ext_applied_note))

        val more = b.card()
        b.navRow(more, getString(R.string.ext_find_more), getString(R.string.ext_find_more_sum),
            android.view.View.OnClickListener { findMore() })
    }

    // ---------- one row ----------
    private fun addCard(content: LinearLayout, e: Extension, recommended: Boolean) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setPadding(dp(14), dp(12), dp(10), dp(12))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(16).toFloat()
        bg.setColor(attr(R.attr.surfaceColor))
        card.background = bg

        card.addView(iconView(e.name), LinearLayout.LayoutParams(dp(38), dp(38)))

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val nm = TextView(this)
        nm.text = e.name
        nm.setTextColor(attr(R.attr.textPrimaryColor))
        nm.textSize = 15f
        nm.setTypeface(null, Typeface.BOLD)
        val ds = TextView(this)
        ds.text = if (e.desc.isNotBlank()) e.desc else e.pattern
        ds.setTextColor(attr(R.attr.textSecondaryColor))
        ds.textSize = 12f
        ds.maxLines = 2
        col.addView(nm)
        col.addView(ds)
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp.leftMargin = dp(12)
        card.addView(col, lp)

        if (recommended) {
            val plus = TextView(this)
            plus.text = "+"
            plus.textSize = 24f
            plus.setTextColor(attr(R.attr.brandColor))
            plus.setPadding(dp(12), dp(2), dp(10), dp(2))
            plus.setOnClickListener { install(e) }
            card.addView(plus)
        } else {
            val sw = androidx.appcompat.widget.SwitchCompat(this)
            sw.isChecked = e.enabled
            sw.setOnCheckedChangeListener { _, on -> store.update(e.copy(enabled = on)) }
            card.addView(sw)
            card.setOnClickListener { detail(e) }
            card.isClickable = true
        }

        val clp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        clp.bottomMargin = dp(10)
        content.addView(card, clp)
    }

    private fun iconView(name: String): TextView {
        val t = TextView(this)
        t.text = (name.trim().firstOrNull() ?: '?').uppercase()
        t.gravity = Gravity.CENTER
        t.setTextColor(Color.WHITE)
        t.textSize = 16f
        t.setTypeface(null, Typeface.BOLD)
        val d = GradientDrawable()
        d.cornerRadius = dp(11).toFloat()
        d.setColor(palette[Math.abs(name.hashCode()) % palette.size])
        t.background = d
        return t
    }

    // ---------- actions ----------
    private fun install(e: Extension) {
        store.add(e.copy(enabled = true))
        toast(getString(R.string.ext_installed_toast, e.name))
        rebuild()
    }

    private fun detail(e: Extension) {
        val items = arrayOf(getString(R.string.ext_edit), getString(R.string.ext_delete))
        AlertDialog.Builder(this)
            .setTitle(e.name)
            .setMessage(if (e.desc.isNotBlank()) e.desc else e.pattern)
            .setItems(items) { _, which ->
                if (which == 0) editDialog(e) else confirmDelete(e)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun findMore() {
        AlertDialog.Builder(this)
            .setTitle(R.string.ext_find_more)
            .setMessage(R.string.ext_find_note)
            .setPositiveButton(R.string.ext_open_repo) { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/vasudev-work/Ekant-Browser")))
                } catch (_: Exception) { }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.ext_name) }
        val pattern = EditText(this).apply { hint = getString(R.string.ext_pattern); setText("*") }
        val code = EditText(this).apply { hint = getString(R.string.ext_code); minLines = 4 }
        box.addView(name); box.addView(pattern); box.addView(code)

        AlertDialog.Builder(this).setTitle(R.string.ext_add_manual).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val n = name.text.toString().trim().ifEmpty { "Extension" }
                store.add(Extension.new(n, pattern.text.toString().trim().ifEmpty { "*" },
                    code.text.toString()))
                toast(getString(R.string.ext_saved))
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun editDialog(e: Extension) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.ext_name); setText(e.name) }
        val pattern = EditText(this).apply { hint = getString(R.string.ext_pattern); setText(e.pattern) }
        val code = EditText(this).apply { hint = getString(R.string.ext_code); setText(e.code); minLines = 4 }
        box.addView(name); box.addView(pattern); box.addView(code)

        AlertDialog.Builder(this).setTitle(R.string.ext_edit).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                store.update(e.copy(
                    name = name.text.toString().trim().ifEmpty { e.name },
                    pattern = pattern.text.toString().trim().ifEmpty { "*" },
                    code = code.text.toString()))
                toast(getString(R.string.ext_saved))
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(e: Extension) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.ext_delete) + ": " + e.name + "?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                store.delete(e.id)
                toast(getString(R.string.ext_deleted))
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun importFrom(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
            var fname = "Script"
            contentResolver.query(uri, null, null, null, null)?.use { c ->
                val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && i >= 0) fname = c.getString(i) ?: "Script"
            }
            if (text.isBlank()) { toast(getString(R.string.ext_empty)); return }
            val name = fname.substringBeforeLast(".").ifEmpty { "Script" }
            store.add(Extension.new(name, "*", text, getString(R.string.ext_uploaded_desc)))
            toast(getString(R.string.ext_loaded))
            rebuild()
        } catch (_: Exception) {
            toast(getString(R.string.ext_load_failed))
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
