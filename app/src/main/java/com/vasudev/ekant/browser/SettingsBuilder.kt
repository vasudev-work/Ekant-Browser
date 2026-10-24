package com.vasudev.ekant.browser

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial

/** Card/row builder shared by the settings screens and the menu (ported from My Browser). */
class SettingsBuilder(private val a: Context, private val root: LinearLayout) {

    private val textColor: Int = attr(R.attr.textPrimaryColor)
    private val mutedColor: Int = attr(R.attr.textSecondaryColor)
    private val accentColor: Int = attr(R.attr.brandColor)

    init { seedIcons() }


    /**
     * Settings rows used to be plain text, 183 of them. Rather than pass an icon
     * at every call site, the builder looks the title up here, so one table gives
     * every screen its icons.
     */
    private val iconByTitle = HashMap<String, Int>()

    private fun seedIcons() {
        fun put(id: Int, icon: Int) { iconByTitle[a.getString(id)] = icon }

        // the settings hub
        put(R.string.s_appearance, R.drawable.ic_palette)
        put(R.string.hp_title, R.drawable.ic_home)
        put(R.string.s_homepage, R.drawable.ic_home)
        put(R.string.s_search_engine, R.drawable.ic_globe)
        put(R.string.s_toolbar_nav, R.drawable.ic_toolbar)
        put(R.string.s_tabs, R.drawable.ic_tabs)
        put(R.string.s_bookmarks, R.drawable.ic_bookmark)
        put(R.string.s_history, R.drawable.ic_history)
        put(R.string.hs_title, R.drawable.ic_schedule)
        put(R.string.rl_title, R.drawable.ic_book)
        put(R.string.fl_title, R.drawable.ic_folder)
        put(R.string.s_downloads, R.drawable.ic_download)
        put(R.string.dls_title, R.drawable.ic_tune)
        put(R.string.s_website_settings, R.drawable.ic_globe)
        put(R.string.rd_title, R.drawable.ic_book)
        put(R.string.r_privacy_dashboard, R.drawable.ic_shield)
        put(R.string.ab_title, R.drawable.ic_block)
        put(R.string.s_tracking, R.drawable.ic_eyeoff)
        put(R.string.s_cookies, R.drawable.ic_cookie)
        put(R.string.s_permissions, R.drawable.ic_verified)
        put(R.string.pf_title, R.drawable.ic_access)
        put(R.string.s_passwords, R.drawable.ic_key)
        put(R.string.r_autofill, R.drawable.ic_note)
        put(R.string.s_clear_data, R.drawable.ic_delete)
        put(R.string.s_data_usage, R.drawable.ic_data)
        put(R.string.s_cache, R.drawable.ic_cache)
        put(R.string.s_background, R.drawable.ic_memory)
        put(R.string.s_text_size, R.drawable.ic_font)
        put(R.string.s_reader_translate, R.drawable.ic_speak)
        put(R.string.s_accessibility, R.drawable.ic_access)
        put(R.string.sf_title, R.drawable.ic_brightness)
        put(R.string.s_backup, R.drawable.ic_cloud)
        put(R.string.s_developer, R.drawable.ic_code)
        put(R.string.dg_title, R.drawable.ic_monitor)
        put(R.string.nl_title, R.drawable.ic_list)
        put(R.string.uf_title, R.drawable.ic_filter)
        put(R.string.s_experimental, R.drawable.ic_tools)
        put(R.string.r_extensions, R.drawable.ic_extension)
        put(R.string.us_title, R.drawable.ic_note)
        put(R.string.s_webview_version, R.drawable.ic_info)
        put(R.string.s_licences, R.drawable.ic_note)
        put(R.string.s_privacy_policy, R.drawable.ic_shield)
        put(R.string.s_feedback, R.drawable.ic_help)
        put(R.string.s_updates, R.drawable.ic_update)
        put(R.string.r_about, R.drawable.ic_info)
        put(R.string.s_clear_history, R.drawable.ic_delete)

        // privacy and shielding
        put(R.string.sh_ads, R.drawable.ic_block)
        put(R.string.sh_trackers, R.drawable.ic_eyeoff)
        put(R.string.sh_cookies, R.drawable.ic_cookie)
        put(R.string.sh_3p, R.drawable.ic_cookie)
        put(R.string.sh_images, R.drawable.ic_movie)
        put(R.string.sh_popups, R.drawable.ic_globe)
        put(R.string.sh_scripts, R.drawable.ic_code)
        put(R.string.sh_fonts, R.drawable.ic_font)
        put(R.string.sh_secure_screen, R.drawable.ic_lock)
        put(R.string.sh_clear_exit, R.drawable.ic_delete)
        put(R.string.sh_clear_close, R.drawable.ic_delete)
        put(R.string.sh_clean_urls, R.drawable.ic_filter)
        put(R.string.sh_easylist, R.drawable.ic_list)
        put(R.string.sh_safe_browsing, R.drawable.ic_verified)
        put(R.string.sh_webrtc, R.drawable.ic_shield)
        put(R.string.sh_app_launch, R.drawable.ic_block)
        put(R.string.sh_bg_media, R.drawable.ic_movie)
        put(R.string.reset_identity, R.drawable.ic_eyeoff)
        put(R.string.priv_clear_all, R.drawable.ic_delete)
        put(R.string.priv_clear_cache, R.drawable.ic_cache)
        put(R.string.priv_clear_cookies, R.drawable.ic_cookie)

        // advanced and appearance
        put(R.string.adv_memory, R.drawable.ic_memory)
        put(R.string.adv_https, R.drawable.ic_shield)
        put(R.string.adv_fp, R.drawable.ic_eyeoff)
        put(R.string.adv_perms, R.drawable.ic_verified)
        put(R.string.adv_swipe, R.drawable.ic_toolbar)
        put(R.string.adv_debug, R.drawable.ic_code)
        put(R.string.adv_amp, R.drawable.ic_globe)
        put(R.string.adv_double_back, R.drawable.ic_logout)
        put(R.string.adv_ua, R.drawable.ic_globe)
        put(R.string.ap_amoled_ui, R.drawable.ic_brightness)
        put(R.string.ap_dynamic, R.drawable.ic_palette)
        put(R.string.ap_force_dark, R.drawable.ic_brightness)
        put(R.string.ap_desktop, R.drawable.ic_desktop)
        put(R.string.ap_external, R.drawable.ic_logout)
        put(R.string.ap_immersive, R.drawable.ic_monitor)
        put(R.string.ap_pull, R.drawable.ic_refresh)
        put(R.string.ap_scrollbars, R.drawable.ic_toolbar)
        put(R.string.ap_volume, R.drawable.ic_speak)
        put(R.string.ap_force_zoom, R.drawable.ic_zoom)
        put(R.string.theme_dark, R.drawable.ic_brightness)
        put(R.string.theme_light, R.drawable.ic_brightness)
        put(R.string.theme_system, R.drawable.ic_settings)
        put(R.string.brand_aekant, R.drawable.ic_palette)
        put(R.string.brand_quiet, R.drawable.ic_palette)

        // reading, start page, downloads, backup, passwords
        put(R.string.rd_amoled, R.drawable.ic_brightness)
        put(R.string.rd_autoscroll, R.drawable.ic_refresh)
        put(R.string.rd_vertical, R.drawable.ic_book)
        put(R.string.rd_paginate, R.drawable.ic_book)
        put(R.string.rd_bionic, R.drawable.ic_font)
        put(R.string.rd_eink, R.drawable.ic_brightness)
        put(R.string.rd_focus, R.drawable.ic_book)
        put(R.string.rd_highlight, R.drawable.ic_speak)
        put(R.string.rd_keep_screen, R.drawable.ic_lock)
        put(R.string.rd_keepalive, R.drawable.ic_memory)
        put(R.string.rd_live, R.drawable.ic_speak)
        put(R.string.rd_retry, R.drawable.ic_refresh)
        put(R.string.rd_battery, R.drawable.ic_memory)
        put(R.string.rd_dict_add, R.drawable.ic_note)
        put(R.string.sf_on, R.drawable.ic_brightness)
        put(R.string.sf_night, R.drawable.ic_brightness)
        put(R.string.hs_greeting, R.drawable.ic_home)
        put(R.string.hs_show_recent, R.drawable.ic_history)
        put(R.string.hs_add, R.drawable.ic_new_tab)
        put(R.string.hs_reset, R.drawable.ic_refresh)
        put(R.string.ntp_bg, R.drawable.ic_palette)
        put(R.string.ntp_topsites, R.drawable.ic_globe)
        put(R.string.ntp_stats, R.drawable.ic_data)
        put(R.string.ntp_new, R.drawable.ic_new_tab)
        put(R.string.dl_fast, R.drawable.ic_download)
        put(R.string.dl_ask, R.drawable.ic_download)
        put(R.string.dls_background, R.drawable.ic_download)
        put(R.string.dls_save_to, R.drawable.ic_folder)
        put(R.string.bk_export, R.drawable.ic_cloud)
        put(R.string.bk_import, R.drawable.ic_cloud)
        put(R.string.pw_save, R.drawable.ic_key)
        put(R.string.pw_auto, R.drawable.ic_key)
        put(R.string.pw_export, R.drawable.ic_cloud)
        put(R.string.af_private, R.drawable.ic_incognito)
        put(R.string.af_browser, R.drawable.ic_settings)
        put(R.string.af_other, R.drawable.ic_share)
        put(R.string.tabs_restore, R.drawable.ic_tabs)
        put(R.string.tabs_private_sep, R.drawable.ic_incognito)
        put(R.string.tb_top, R.drawable.ic_toolbar)
        put(R.string.tb_bottom, R.drawable.ic_toolbar)
        put(R.string.tb_compact, R.drawable.ic_toolbar)
        put(R.string.tb_focus, R.drawable.ic_toolbar)
        put(R.string.nl_clear, R.drawable.ic_delete)
        put(R.string.nl_enable, R.drawable.ic_list)
        put(R.string.uf_add, R.drawable.ic_filter)
        put(R.string.uf_enable, R.drawable.ic_filter)
        put(R.string.us_add, R.drawable.ic_note)
        put(R.string.ext_delete, R.drawable.ic_delete)
        put(R.string.ext_edit, R.drawable.ic_note)
        put(R.string.ext_find_more, R.drawable.ic_extension)
        put(R.string.ext_paste_url, R.drawable.ic_extension)
        put(R.string.ext_remote_source, R.drawable.ic_globe)
        put(R.string.media_copy, R.drawable.ic_movie)
        put(R.string.media_download, R.drawable.ic_download)
        put(R.string.lang_add, R.drawable.ic_globe)
        put(R.string.lang_device, R.drawable.ic_globe)
        put(R.string.lang_translate, R.drawable.ic_translate)
        put(R.string.se_add, R.drawable.ic_globe)
        put(R.string.se_current, R.drawable.ic_globe)
        put(R.string.se_set_default, R.drawable.ic_globe)
        put(R.string.pf_restricted, R.drawable.ic_shield)
        put(R.string.pf_standard, R.drawable.ic_access)
        put(R.string.pf_trusted, R.drawable.ic_verified)
        put(R.string.pf_change, R.drawable.ic_access)
        put(R.string.st_manage_modes, R.drawable.ic_access)
        put(R.string.m_page_scripts, R.drawable.ic_code)
        put(R.string.m_block_element, R.drawable.ic_select)

        // diagnostics and files
        for (id in intArrayOf(R.string.dg_android, R.string.dg_battery, R.string.dg_crash,
                R.string.dg_engines, R.string.dg_history, R.string.dg_memory,
                R.string.dg_rules, R.string.dg_service, R.string.dg_tts, R.string.dg_webview)) {
            put(id, R.drawable.ic_monitor)
        }
        for (id in intArrayOf(R.string.fl_audio, R.string.fl_available, R.string.fl_docs,
                R.string.fl_images, R.string.fl_total, R.string.fl_videos)) {
            put(id, R.drawable.ic_folder)
        }
        for (id in intArrayOf(R.string.ab_ads, R.string.ab_blocked, R.string.ab_enable,
                R.string.ab_reset, R.string.ab_saved, R.string.ab_trackers, R.string.ab_add_site)) {
            put(id, R.drawable.ic_block)
        }
        for (id in intArrayOf(R.string.hist_day, R.string.hist_hour, R.string.hist_week)) {
            put(id, R.drawable.ic_schedule)
        }
        for (id in intArrayOf(R.string.hs_style_personal, R.string.hs_style_dashboard,
                R.string.hs_style_minimal)) {
            put(id, R.drawable.ic_home)
        }
    }

    /** Adds the leading icon for a row, if this title has one. */
    private fun titleIcon(row: LinearLayout, title: String) {
        val ic = iconByTitle[title] ?: return
        addIcon(row, ic)
    }

    private fun attr(resid: Int): Int {
        val tv = TypedValue()
        a.theme.resolveAttribute(resid, tv, true)
        return tv.data
    }

    private fun dp(v: Int) = (a.resources.displayMetrics.density * v).toInt()

    fun section(title: String) {
        val t = TextView(a)
        t.text = title
        t.setTextColor(accentColor)
        t.textSize = 13f
        t.letterSpacing = 0.06f
        t.setPadding(dp(22), dp(20), dp(8), dp(8))
        root.addView(t)
    }

    fun card(): LinearLayout {
        val c = LinearLayout(a)
        c.orientation = LinearLayout.VERTICAL
        c.setBackgroundResource(R.drawable.card_bg)
        c.elevation = dp(2).toFloat()
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.leftMargin = dp(16)
        lp.rightMargin = dp(16)
        c.layoutParams = lp
        root.addView(c)
        return c
    }

    fun note(text: String) {
        val t = TextView(a)
        t.text = text
        t.setTextColor(mutedColor)
        t.textSize = 13f
        t.setPadding(dp(26), dp(10), dp(26), dp(0))
        root.addView(t)
    }

    private fun rowBase(card: LinearLayout): LinearLayout {
        val r = LinearLayout(a)
        r.orientation = LinearLayout.HORIZONTAL
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(16), dp(15), dp(16), dp(15))
        card.addView(r)
        return r
    }

    private fun textCol(row: LinearLayout, title: String, summary: String?, color: Int) {
        val col = LinearLayout(a)
        col.orientation = LinearLayout.VERTICAL
        col.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        val t = TextView(a)
        t.text = title
        t.setTextColor(color)
        t.textSize = 15f
        t.maxLines = 1
        t.ellipsize = android.text.TextUtils.TruncateAt.END
        col.addView(t)
        if (!summary.isNullOrEmpty()) {
            val s = TextView(a)
            s.text = summary
            s.setTextColor(mutedColor)
            s.textSize = 13f
            s.maxLines = 2
            s.ellipsize = android.text.TextUtils.TruncateAt.END
            s.setPadding(0, dp(3), 0, 0)
            col.addView(s)
        }
        row.addView(col)
    }

    private fun addIcon(row: LinearLayout, iconRes: Int) {
        val ic = ImageView(a)
        ic.setImageResource(iconRes)
        ic.setColorFilter(mutedColor)
        val ilp = LinearLayout.LayoutParams(dp(22), dp(22))
        ilp.rightMargin = dp(16)
        ic.layoutParams = ilp
        row.addView(ic)
    }

    private fun addChevron(row: LinearLayout) {
        val chev = ImageView(a)
        chev.setImageResource(R.drawable.ic_chev)
        chev.setColorFilter(mutedColor)
        chev.layoutParams = LinearLayout.LayoutParams(dp(18), dp(18))
        row.addView(chev)
    }

    fun divider(card: LinearLayout) {
        val v = View(a)
        v.setBackgroundColor(0x1A808080)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
        lp.leftMargin = dp(16)
        lp.rightMargin = dp(16)
        v.layoutParams = lp
        card.addView(v)
    }

    fun navRow(card: LinearLayout, title: String, summary: String?, l: View.OnClickListener?) {
        val r = rowBase(card)
        titleIcon(r, title)
        textCol(r, title, summary, textColor)
        addChevron(r)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun iconRow(card: LinearLayout, iconRes: Int, title: String, summary: String?, l: View.OnClickListener?) {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, summary, textColor)
        addChevron(r)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    /** Same as iconRow but without the trailing chevron, for the floating menu card. */
    fun menuRow(card: LinearLayout, iconRes: Int, title: String, summary: String?, l: View.OnClickListener?) {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, summary, textColor)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun iconValueRow(card: LinearLayout, iconRes: Int, title: String, value: String, l: View.OnClickListener?) {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, null, textColor)
        val v = TextView(a)
        v.text = value
        v.setTextColor(accentColor)
        v.textSize = 13f
        r.addView(v)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun disabledRow(card: LinearLayout, title: String, summary: String?) {
        val r = rowBase(card)
        textCol(r, title, summary, mutedColor)
        val tag = TextView(a)
        tag.text = "ENGINE"
        tag.textSize = 11f
        tag.letterSpacing = 0.08f
        tag.setTextColor(mutedColor)
        tag.setBackgroundResource(R.drawable.tag_bg)
        tag.setPadding(dp(8), dp(3), dp(8), dp(3))
        r.addView(tag)
        r.isEnabled = false
        r.alpha = 0.6f
    }

    fun switchRow(card: LinearLayout, title: String, summary: String?, checked: Boolean,
                  l: CompoundButton.OnCheckedChangeListener?): SwitchMaterial {
        val r = rowBase(card)
        titleIcon(r, title)
        textCol(r, title, summary, textColor)
        val sw = SwitchMaterial(a)
        sw.isChecked = checked
        if (l != null) sw.setOnCheckedChangeListener(l)
        r.addView(sw)
        return sw
    }

    fun iconSwitchRow(card: LinearLayout, iconRes: Int, title: String, summary: String?,
                      checked: Boolean, l: CompoundButton.OnCheckedChangeListener?): SwitchMaterial {
        val r = rowBase(card)
        addIcon(r, iconRes)
        textCol(r, title, summary, textColor)
        val sw = SwitchMaterial(a)
        sw.isChecked = checked
        if (l != null) sw.setOnCheckedChangeListener(l)
        r.addView(sw)
        return sw
    }

    fun radioRow(card: LinearLayout, title: String, summary: String?, checked: Boolean, l: View.OnClickListener?) {
        val r = rowBase(card)
        titleIcon(r, title)
        val dot = ImageView(a)
        dot.setImageResource(if (checked) R.drawable.radio_on else R.drawable.radio_off)
        val dlp = LinearLayout.LayoutParams(dp(22), dp(22))
        dlp.rightMargin = dp(14)
        dot.layoutParams = dlp
        r.addView(dot)
        textCol(r, title, summary, textColor)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }

    fun valueRow(card: LinearLayout, title: String, value: String, l: View.OnClickListener?) {
        val r = rowBase(card)
        titleIcon(r, title)
        textCol(r, title, null, textColor)
        val v = TextView(a)
        v.text = value
        v.setTextColor(accentColor)
        v.textSize = 13f
        r.addView(v)
        r.setOnClickListener(l)
        r.isClickable = l != null
    }
}
