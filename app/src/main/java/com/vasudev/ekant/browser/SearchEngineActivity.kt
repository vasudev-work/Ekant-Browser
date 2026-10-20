package com.vasudev.ekant.browser

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Add / edit / delete search engines and pick the default. */
class SearchEngineActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_search

    private val store by lazy { SearchEngineStore(this) }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val all = store.getAll()
        b.section(getString(R.string.se_default))
        val d = b.card()
        b.valueRow(d, getString(R.string.se_current), store.default().name, null)
        b.note(getString(R.string.se_note))

        b.section(getString(R.string.se_all))
        for (e in all) {
            val card = b.card()
            val isDefault = e.name == store.default().name
            b.valueRow(card, (if (isDefault) "\u2713 " else "") + e.name, e.template, null)
            b.divider(card)
            b.navRow(card, getString(R.string.se_set_default), null) {
                store.defaultName = e.name
                prefs.edit().putString("se", e.template).apply()
                rebuild()
            }
            b.divider(card)
            b.navRow(card, getString(R.string.ext_edit), null) { editDialog(e) }
            b.divider(card)
            b.navRow(card, getString(R.string.ext_delete), null) { confirmDelete(e) }
        }

        b.section(getString(R.string.ext_add))
        val add = b.card()
        b.navRow(add, getString(R.string.se_add), getString(R.string.se_add_sum)) { addDialog() }
    }

    private fun addDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.se_name) }
        val url = EditText(this).apply { hint = getString(R.string.se_url_hint) }
        box.addView(name); box.addView(url)
        AlertDialog.Builder(this).setTitle(R.string.se_add).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val n = name.text.toString().trim()
                val u = url.text.toString().trim()
                if (n.isEmpty() || u.isEmpty()) return@setPositiveButton
                store.add(SearchEngine(n, u))
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun editDialog(e: SearchEngine) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val name = EditText(this).apply { hint = getString(R.string.se_name); setText(e.name) }
        val url = EditText(this).apply { hint = getString(R.string.se_url_hint); setText(e.template) }
        box.addView(name); box.addView(url)
        AlertDialog.Builder(this).setTitle(R.string.ext_edit).setView(box)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                val n = name.text.toString().trim().ifEmpty { e.name }
                val u = url.text.toString().trim().ifEmpty { e.template }
                store.update(e.name, SearchEngine(n, u))
                if (store.defaultName == e.name) store.defaultName = n
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(e: SearchEngine) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.ext_delete) + ": " + e.name + "?")
            .setPositiveButton(android.R.string.ok) { _, _ -> store.delete(e.name); rebuild() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
