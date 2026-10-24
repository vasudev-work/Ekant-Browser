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

    private var remote: List<ExtStore.Remote>? = null
    private var remoteError: String? = null
    private var remoteLoading = false

    private fun storeBase(): String =
        prefs.getString("ext_store_base", ExtStore.DEFAULT_BASE) ?: ExtStore.DEFAULT_BASE

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

        // ---------- remote store (your GitHub) ----------
        b.section(getString(R.string.ext_remote))
        val rc = b.card()
        b.navRow(rc, getString(R.string.ext_remote_source), shortBase(),
            android.view.View.OnClickListener { editBaseDialog() })
        b.divider(rc)
        b.navRow(rc, if (remoteLoading) getString(R.string.ext_checking) else getString(R.string.ext_check),
            getString(R.string.ext_check_sum), android.view.View.OnClickListener { checkStore() })
        b.divider(rc)
        b.navRow(rc, getString(R.string.ext_paste_url), getString(R.string.ext_paste_url_sum),
            android.view.View.OnClickListener { pasteDialog() })

        if (remoteLoading) b.note(getString(R.string.ext_checking))
        remoteError?.let { b.note(getString(R.string.ext_remote_error, it)) }
        remote?.let { list ->
            if (list.isEmpty()) {
                b.note(getString(R.string.ext_remote_empty))
            } else {
                for (r in list) addCard(content, r, haveIds.contains(r.id))
            }
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
        ds.text = (if (e.desc.isNotBlank()) e.desc else e.pattern) +
            (if (e.isStart()) "  \u00b7  runs first" else "") +
            (if (e.grants.any { it != "none" }) "  \u00b7  GM" else "")
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

    /** Pull the userscript metadata block out and use it for anything not given. */
    private fun withMeta(name: String, pattern: String, code: String, desc: String): Extension {
        val m = UserScriptMeta.parse(code)
        val p = if (pattern.isBlank() || pattern == "*") m.pattern.ifBlank { "*" } else pattern
        return Extension.new(
            name.ifBlank { m.name ?: "Extension" },
            p, code,
            desc.ifBlank { m.desc ?: "" },
            m.runAt, m.grants
        )
    }

    // ---------- remote ----------
    private fun shortBase(): String {
        val b = storeBase()
        return if (b.length > 46) b.take(22) + "..." + b.takeLast(20) else b
    }

    private fun checkStore() {
        remoteLoading = true
        remoteError = null
        rebuild()
        ExtStore.fetchIndex(storeBase()) { list, err ->
            runOnUiThread {
                remoteLoading = false
                remote = list
                remoteError = err
                rebuild()
            }
        }
    }

    private fun installRemote(r: ExtStore.Remote, already: Boolean) {
        if (already) { toast(getString(R.string.ext_already)); return }
        toast(getString(R.string.ext_downloading, r.name))
        ExtStore.fetchScript(storeBase(), r.file) { code, err ->
            runOnUiThread {
                if (code == null) { toast(err ?: getString(R.string.ext_load_failed)); return@runOnUiThread }
                val meta = UserScriptMeta.parse(code)
                store.add(
                    Extension(
                        id = r.id, name = r.name,
                        pattern = r.pattern.ifBlank { meta.pattern.ifBlank { "*" } },
                        code = code, enabled = true, builtIn = false,
                        desc = r.desc.ifBlank { meta.desc ?: getString(R.string.ext_from_github) },
                        runAt = meta.runAt, grants = meta.grants)
                )
                toast(getString(R.string.ext_installed_toast, r.name))
                rebuild()
            }
        }
    }

    private fun pasteDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.ext_name) }
        val url = EditText(this).apply {
            hint = "https://.../my-script.js"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        val pattern = EditText(this).apply { hint = getString(R.string.ext_pattern); setText("*") }
        box.addView(name); box.addView(url); box.addView(pattern)
        AlertDialog.Builder(this).setTitle(R.string.ext_paste_url).setView(box)
            .setPositiveButton(R.string.media_download) { _, _ ->
                val u = url.text.toString().trim()
                if (u.startsWith("http")) {
                    val nm = name.text.toString().trim().ifEmpty { u.substringAfterLast("/").substringBefore(".") }
                    toast(getString(R.string.ext_downloading, nm))
                    Thread {
                        try {
                            val c = java.net.URL(u).openConnection() as java.net.HttpURLConnection
                            c.connectTimeout = 10000
                            c.readTimeout = 15000
                            c.setRequestProperty("User-Agent", "Aekant")
                            val code = c.inputStream.bufferedReader().readText()
                            runOnUiThread {
                                if (code.isBlank()) { toast(getString(R.string.ext_empty)); return@runOnUiThread }
                                store.add(withMeta(nm, pattern.text.toString().trim(),
                                    code, getString(R.string.ext_from_url)))
                                toast(getString(R.string.ext_installed_toast, nm))
                                rebuild()
                            }
                        } catch (_: Exception) {
                            runOnUiThread { toast(getString(R.string.ext_load_failed)) }
                        }
                    }.start()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun editBaseDialog() {
        val e = EditText(this)
        e.setText(storeBase())
        e.inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
        AlertDialog.Builder(this).setTitle(R.string.ext_remote_source).setView(e)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                prefs.edit().putString("ext_store_base", e.text.toString().trim()).apply()
                remote = null; remoteError = null
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addCard(content: LinearLayout, r: ExtStore.Remote, already: Boolean) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setPadding(dp(14), dp(12), dp(10), dp(12))
        val bg = GradientDrawable()
        bg.cornerRadius = dp(16).toFloat()
        bg.setColor(attr(R.attr.surfaceColor))
        card.background = bg
        card.addView(iconView(r.name), LinearLayout.LayoutParams(dp(38), dp(38)))
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val nm = TextView(this)
        nm.text = r.name
        nm.setTextColor(attr(R.attr.textPrimaryColor))
        nm.textSize = 15f
        nm.setTypeface(null, Typeface.BOLD)
        val ds = TextView(this)
        ds.text = (if (r.desc.isNotBlank()) r.desc else r.pattern) + "  \u00b7  v" + r.version
        ds.setTextColor(attr(R.attr.textSecondaryColor))
        ds.textSize = 12f
        ds.maxLines = 2
        col.addView(nm); col.addView(ds)
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp.leftMargin = dp(12)
        card.addView(col, lp)
        val btn = TextView(this)
        btn.text = if (already) getString(R.string.ext_installed_mark) else "+"
        btn.textSize = if (already) 12f else 24f
        btn.setTextColor(if (already) attr(R.attr.textSecondaryColor) else attr(R.attr.brandColor))
        btn.setPadding(dp(12), dp(2), dp(10), dp(2))
        btn.setOnClickListener { installRemote(r, already) }
        card.addView(btn)
        val clp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        clp.bottomMargin = dp(10)
        content.addView(card, clp)
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
        val earlyAdd = android.widget.CheckBox(this).apply { text = getString(R.string.ext_run_start) }
        box.addView(name); box.addView(pattern); box.addView(code); box.addView(earlyAdd)

        AlertDialog.Builder(this).setTitle(R.string.ext_add_manual).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val n = name.text.toString().trim()
                store.add(withMeta(n, pattern.text.toString().trim(), code.text.toString(), "")
                    .copy(runAt = if (earlyAdd.isChecked) Extension.RUN_START else Extension.RUN_END))
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
        val earlyEdit = android.widget.CheckBox(this).apply {
            text = getString(R.string.ext_run_start); isChecked = e.isStart()
        }
        box.addView(name); box.addView(pattern); box.addView(code); box.addView(earlyEdit)

        AlertDialog.Builder(this).setTitle(R.string.ext_edit).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val m = UserScriptMeta.parse(code.text.toString())
                store.update(e.copy(
                    name = name.text.toString().trim().ifEmpty { e.name },
                    pattern = pattern.text.toString().trim().ifEmpty { m.pattern.ifBlank { "*" } },
                    code = code.text.toString(),
                    desc = e.desc.ifBlank { m.desc ?: "" },
                    grants = if (m.grants.isEmpty()) e.grants else m.grants,
                    runAt = if (earlyEdit.isChecked) Extension.RUN_START else Extension.RUN_END))
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
            store.add(withMeta(name, "", text, getString(R.string.ext_uploaded_desc)))
            toast(getString(R.string.ext_loaded))
            rebuild()
        } catch (_: Exception) {
            toast(getString(R.string.ext_load_failed))
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
