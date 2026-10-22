package com.vasudev.ekant.browser

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

/** Settings hub with a search box and a Manage Modes card, as the design asks. */
class SettingsActivity : BaseSettingsActivity() {

    override val titleRes = R.string.settings_title

    private var query = ""
    private var searchAdded = false

    private data class Item(val label: String, val action: () -> Unit)

    private fun sections(): List<Pair<String, List<Item>>> = listOf(
        getString(R.string.sec_general) to listOf(
            Item(getString(R.string.s_appearance)) { go(AppearanceActivity::class.java) },
            Item(getString(R.string.s_homepage)) { go(HomeShortcutsActivity::class.java) },
            Item(getString(R.string.s_search_engine)) { go(SearchEngineActivity::class.java) },
            Item(getString(R.string.s_toolbar_nav)) { go(ToolbarActivity::class.java) }
        ),
        getString(R.string.sec_browsing) to listOf(
            Item(getString(R.string.s_tabs)) { go(TabsActivity::class.java) },
            Item(getString(R.string.s_bookmarks)) { go(BookmarksActivity::class.java) },
            Item(getString(R.string.s_history)) { go(HistoryActivity::class.java) },
            Item(getString(R.string.rl_title)) { go(ReadingListActivity::class.java) },
            Item(getString(R.string.fl_title)) { go(FilesActivity::class.java) },
            Item(getString(R.string.s_downloads)) {
                try { startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)) } catch (_: Exception) { }
            },
            Item(getString(R.string.s_website_settings)) { go(AdvancedActivity::class.java) },
            Item(getString(R.string.rd_title)) { go(ReadingActivity::class.java) }
        ),
        getString(R.string.sec_privacy) to listOf(
            Item(getString(R.string.r_privacy_dashboard)) { go(PrivacyActivity::class.java) },
            Item(getString(R.string.ab_title)) { go(AdBlockActivity::class.java) },
            Item(getString(R.string.s_tracking)) { go(ShieldsActivity::class.java) },
            Item(getString(R.string.s_cookies)) { go(ShieldsActivity::class.java) },
            Item(getString(R.string.s_permissions)) { go(SitePermissionsActivity::class.java) },
            Item(getString(R.string.pf_title)) { go(ProfilesActivity::class.java) },
            Item(getString(R.string.s_passwords)) { go(PasswordsActivity::class.java) },
            Item(getString(R.string.r_autofill)) { go(AutofillActivity::class.java) },
            Item(getString(R.string.s_clear_data)) { go(PrivacyActivity::class.java) }
        ),
        getString(R.string.sec_performance) to listOf(
            Item(getString(R.string.s_data_usage)) { go(ShieldsActivity::class.java) },
            Item(getString(R.string.s_cache)) { go(PrivacyActivity::class.java) },
            Item(getString(R.string.s_background)) { go(AdvancedActivity::class.java) }
        ),
        getString(R.string.sec_accessibility) to listOf(
            Item(getString(R.string.s_text_size)) { go(AppearanceActivity::class.java) },
            Item(getString(R.string.s_reader_translate)) {
                showDialog(getString(R.string.s_reader_translate), getString(R.string.reader_note))
            },
            Item(getString(R.string.s_accessibility)) { go(AppearanceActivity::class.java) }
        ),
        getString(R.string.sec_advanced) to listOf(
            Item(getString(R.string.s_backup)) { go(BackupActivity::class.java) },
            Item(getString(R.string.s_developer)) { go(AdvancedActivity::class.java) },
            Item(getString(R.string.dg_title)) { go(DiagnosticsActivity::class.java) },
            Item(getString(R.string.nl_title)) { go(NetLogActivity::class.java) },
            Item(getString(R.string.uf_title)) { go(UrlFiltersActivity::class.java) },
            Item(getString(R.string.s_experimental)) {
                showDialog(getString(R.string.s_experimental), getString(R.string.exp_note))
            },
            Item(getString(R.string.r_extensions)) { go(ExtensionsActivity::class.java) },
            Item(getString(R.string.us_title)) { go(UserStylesActivity::class.java) }
        ),
        getString(R.string.sec_about) to listOf(
            Item(getString(R.string.s_webview_version)) {
                showDialog(getString(R.string.s_webview_version), webViewVersion())
            },
            Item(getString(R.string.s_licences)) {
                showDialog(getString(R.string.s_licences), getString(R.string.licences_note))
            },
            Item(getString(R.string.s_privacy_policy)) {
                showDialog(getString(R.string.s_privacy_policy), getString(R.string.policy_note))
            },
            Item(getString(R.string.s_feedback)) { feedback() },
            Item(getString(R.string.s_updates)) {
                showDialog(getString(R.string.s_updates), getString(R.string.updates_note))
            },
            Item(getString(R.string.r_about)) { go(AboutActivity::class.java) }
        )
    )

    override fun build(b: SettingsBuilder, content: LinearLayout) {
        if (!searchAdded) {
            searchAdded = true
            val et = android.widget.EditText(this)
            et.hint = getString(R.string.st_search)
            et.isSingleLine = true
            et.setTextColor(attr(R.attr.textPrimaryColor))
            et.setHintTextColor(attr(R.attr.textSecondaryColor))
            val bg = android.graphics.drawable.GradientDrawable()
            bg.cornerRadius = dp(20).toFloat()
            bg.setColor(attr(R.attr.surface2Color))
            et.background = bg
            et.setPadding(dp(16), dp(11), dp(16), dp(11))
            et.layoutParams = LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) { query = s?.toString() ?: ""; rebuild() }
                override fun beforeTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) { }
                override fun onTextChanged(s: CharSequence?, a: Int, b2: Int, c: Int) { }
            })
            val box = LinearLayout(this)
            box.setPadding(dp(14), dp(10), dp(14), dp(2))
            box.addView(et)
            addTopView(box)
        }

        if (query.isNotBlank()) {
            b.section(getString(R.string.st_search_results))
            val card = b.card()
            var first = true
            for ((_, items) in sections()) {
                for (item in items) {
                    if (!item.label.contains(query, true)) continue
                    if (!first) b.divider(card)
                    first = false
                    b.navRow(card, item.label, null, android.view.View.OnClickListener { item.action() })
                }
            }
            if (first) b.note(getString(R.string.d_empty))
            return
        }

        b.section(getString(R.string.st_modes))
        val m = b.card()
        b.navRow(m, getString(R.string.st_manage_modes), containerName(),
            android.view.View.OnClickListener {
                try {
                    startActivity(Intent(this, MainActivity::class.java)
                        .putExtra("show", "containers")
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                } catch (_: Exception) { }
            })

        for ((title, items) in sections()) {
            b.section(title)
            val card = b.card()
            for ((i, item) in items.withIndex()) {
                if (i > 0) b.divider(card)
                b.navRow(card, item.label, null, android.view.View.OnClickListener { item.action() })
            }
        }

        b.section(getString(R.string.sec_about))
        val av = b.card()
        b.valueRow(av, getString(R.string.s_app_version), appVersion(), null)
    }

    private fun containerName(): String = try {
        val s = ContainerStore(this)
        val id = s.activeContainerId
        s.getContainers().firstOrNull { it.id == id }?.name ?: "Personal"
    } catch (_: Exception) { "Personal" }

    private fun go(cls: Class<*>) { startActivity(Intent(this, cls)) }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setPositiveButton(android.R.string.ok, null).show()
    }

    private fun appVersion(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
    } catch (_: Exception) { "?" }

    private fun webViewVersion(): String = try {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val pm = android.webkit.WebView.getCurrentWebViewPackage()
            (pm?.versionName ?: "unknown") + "  (" + (pm?.packageName ?: "") + ")"
        } else "unknown"
    } catch (_: Exception) { "unknown" }

    private fun feedback() {
        val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getString(R.string.feedback_email)))
        try { startActivity(i) }
        catch (_: Exception) { showDialog(getString(R.string.s_feedback), getString(R.string.feedback_email)) }
    }
}
