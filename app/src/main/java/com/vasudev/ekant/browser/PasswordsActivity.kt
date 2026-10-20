package com.vasudev.ekant.browser

import android.provider.OpenableColumns
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class PasswordsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_passwords

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            var name = "file"
            contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && idx >= 0) name = c.getString(idx)
            }
            Toast.makeText(this, "File selected: " + name, Toast.LENGTH_SHORT).show()
        }
    }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        val c1 = b.card()
        b.switchRow(c1, getString(R.string.pw_save), getString(R.string.pw_save_sum),
            prefs.getBoolean("pw_save", true)) { _, on ->
            prefs.edit().putBoolean("pw_save", on).apply()
        }
        b.divider(c1)
        b.switchRow(c1, getString(R.string.pw_auto), getString(R.string.pw_auto_sum),
            prefs.getBoolean("pw_auto", false)) { _, on ->
            prefs.edit().putBoolean("pw_auto", on).apply()
        }

        val c2 = b.card()
        b.iconRow(c2, R.drawable.ic_folder, getString(R.string.pw_import),
            getString(R.string.pw_import_sum)) {
            pickFile.launch(arrayOf("*/*"))
        }
        b.divider(c2)
        b.navRow(c2, getString(R.string.pw_export), getString(R.string.pw_export_sum), null)

        b.section(getString(R.string.pw_list))
        val c3 = b.card()
        b.navRow(c3, getString(R.string.pw_never), null, null)
    }
}
