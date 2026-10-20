package com.vasudev.ekant.browser

import android.net.Uri
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject

/** Export / import all local settings as a JSON file (no cloud). */
class BackupActivity : BaseSettingsActivity() {

    override val titleRes = R.string.r_backup

    private val importFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) importFrom(uri)
        }

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        b.section(getString(R.string.bk_section))
        val c = b.card()
        b.navRow(c, getString(R.string.bk_export), getString(R.string.bk_export_sum)) { export() }
        b.divider(c)
        b.navRow(c, getString(R.string.bk_import), getString(R.string.bk_import_sum)) {
            importFile.launch(arrayOf("application/json", "*/*"))
        }
        b.note(getString(R.string.bk_note))
    }

    private val stores = arrayOf("aekant", "aekant_containers", "aekant_extensions")

    private fun export() {
        try {
            val root = JSONObject()
            for (name in stores) {
                val p = getSharedPreferences(name, MODE_PRIVATE)
                val o = JSONObject()
                for ((k, v) in p.all) o.put(k, v.toString())
                root.put(name, o)
            }
            val dir = java.io.File(filesDir, "backup")
            dir.mkdirs()
            val f = java.io.File(dir, "aekant-backup-" + System.currentTimeMillis() + ".json")
            f.writeText(root.toString(2))
            toast(getString(R.string.bk_exported) + ": " + f.name)
        } catch (_: Exception) {
            toast(getString(R.string.bk_failed))
        }
    }

    private fun importFrom(uri: Uri) {
        try {
            val txt = contentResolver.openInputStream(uri)
                ?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
            val root = JSONObject(txt)
            for (name in stores) {
                if (!root.has(name)) continue
                val o = root.getJSONObject(name)
                val ed = getSharedPreferences(name, MODE_PRIVATE).edit()
                val it = o.keys()
                while (it.hasNext()) {
                    val k = it.next()
                    val v = o.getString(k)
                    when {
                        v == "true" || v == "false" -> ed.putBoolean(k, v == "true")
                        v.toIntOrNull() != null -> ed.putInt(k, v.toInt())
                        else -> ed.putString(k, v)
                    }
                }
                ed.apply()
            }
            toast(getString(R.string.bk_imported))
            rebuild()
        } catch (_: Exception) {
            toast(getString(R.string.bk_failed))
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}
