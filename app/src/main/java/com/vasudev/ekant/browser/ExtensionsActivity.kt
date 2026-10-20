package com.vasudev.ekant.browser

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

/** Manage extensions: enable/disable, edit, delete, add manually, upload a script. */
class ExtensionsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_extensions

    private val store by lazy { ExtensionStore(this) }

    private val importFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) importFrom(uri)
        }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val list = store.getAll()

        b.section(getString(R.string.ext_installed))
        if (list.isEmpty()) {
            b.note(getString(R.string.ext_none))
        } else {
            for (e in list) {
                val card = b.card()
                b.switchRow(card, e.name, e.pattern, e.enabled) { _, on ->
                    store.update(e.copy(enabled = on))
                }
                b.divider(card)
                b.navRow(card, getString(R.string.ext_edit), null) { editDialog(e) }
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) { confirmDelete(e) }
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
                store.add(
                    Extension.new(
                        n,
                        pattern.text.toString().trim().ifEmpty { "*" },
                        code.text.toString()
                    )
                )
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
                store.update(
                    e.copy(
                        name = name.text.toString().trim().ifEmpty { e.name },
                        pattern = pattern.text.toString().trim().ifEmpty { "*" },
                        code = code.text.toString()
                    )
                )
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
            if (text.isBlank()) {
                toast(getString(R.string.ext_empty))
                return
            }
            val name = fname.substringBeforeLast(".").ifEmpty { "Script" }
            store.add(Extension.new(name, "*", text))
            toast(getString(R.string.ext_loaded))
            rebuild()
        } catch (_: Exception) {
            toast(getString(R.string.ext_load_failed))
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
