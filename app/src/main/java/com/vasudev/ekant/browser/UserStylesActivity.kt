package com.vasudev.ekant.browser

import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Manage user CSS rules, applied per site pattern. */
class UserStylesActivity : BaseSettingsActivity() {

    override val titleRes = R.string.us_title

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val list = UserStyleStore.all(this)
        b.section(getString(R.string.us_title))
        if (list.isEmpty()) {
            b.note(getString(R.string.us_empty))
        } else {
            for (i in list.indices) {
                val t = list[i]
                val card = b.card()
                b.switchRow(card, t.first, t.second.replace("\n", " ").take(44), t.third) { _, on ->
                    UserStyleStore.setEnabled(this, i, on); rebuild()
                }
                b.divider(card)
                b.navRow(card, getString(R.string.ext_delete), null) {
                    UserStyleStore.delete(this, i); rebuild()
                }
            }
        }
        b.section(getString(R.string.ext_add))
        val c = b.card()
        b.navRow(c, getString(R.string.us_add), getString(R.string.us_add_sum)) { addDialog() }
    }

    private fun addDialog() {
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(48, 24, 48, 8)
        val p = EditText(this)
        p.hint = getString(R.string.us_pattern)
        col.addView(p)
        val css = EditText(this)
        css.hint = getString(R.string.us_css)
        css.minLines = 4
        css.gravity = Gravity.TOP
        col.addView(css)
        AlertDialog.Builder(this)
            .setTitle(R.string.us_add)
            .setView(col)
            .setPositiveButton(R.string.ext_save) { _, _ ->
                UserStyleStore.add(this, p.text.toString().trim(), css.text.toString())
                rebuild()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
