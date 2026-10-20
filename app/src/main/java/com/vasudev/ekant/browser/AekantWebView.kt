package com.vasudev.ekant.browser

import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView

/**
 * WebView with a richer text-selection menu (Search / Share / Translate / Ask AI)
 * and a long-press menu for links and images.
 */
class AekantWebView(context: Context) : WebView(context) {

    /** (action, selectedText) where action is one of search/share/translate/ai */
    var onTextAction: ((String, String) -> Unit)? = null

    /** (url, isImage) */
    var onLinkLongPress: ((String, Boolean) -> Unit)? = null

    init {
        setOnLongClickListener {
            val hit = hitTestResult ?: return@setOnLongClickListener false
            val url = hit.extra
            when (hit.type) {
                HitTestResult.SRC_ANCHOR_TYPE, HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    if (url.isNullOrBlank()) false else { onLinkLongPress?.invoke(url, false); true }
                }
                HitTestResult.IMAGE_TYPE -> {
                    if (url.isNullOrBlank()) false else { onLinkLongPress?.invoke(url, true); true }
                }
                else -> false
            }
        }
    }

    override fun startActionMode(callback: ActionMode.Callback): ActionMode? =
        super.startActionMode(wrap(callback))

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? =
        super.startActionMode(wrap(callback), type)

    private fun wrap(cb: ActionMode.Callback) = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            if (!cb.onCreateActionMode(mode, menu)) return false
            menu.add(Menu.NONE, ID_SEARCH, 100, "Search")
            menu.add(Menu.NONE, ID_SHARE, 101, "Share")
            menu.add(Menu.NONE, ID_TRANSLATE, 102, "Translate")
            menu.add(Menu.NONE, ID_AI, 103, "Ask AI")
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean =
            cb.onPrepareActionMode(mode, menu)

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                ID_SEARCH -> { selected("search"); return true }
                ID_SHARE -> { selected("share"); return true }
                ID_TRANSLATE -> { selected("translate"); return true }
                ID_AI -> { selected("ai"); return true }
            }
            return cb.onActionItemClicked(mode, item)
        }

        override fun onDestroyActionMode(mode: ActionMode) { cb.onDestroyActionMode(mode) }
    }

    private fun selected(action: String) {
        evaluateJavascript("(function(){try{return window.getSelection().toString();}catch(e){return '';}})()") { js ->
            val text = try { org.json.JSONTokener(js).nextValue() as? String ?: "" } catch (_: Exception) { "" }
            if (text.isNotBlank()) onTextAction?.invoke(action, text)
        }
    }

    companion object {
        const val ID_SEARCH = 1
        const val ID_SHARE = 2
        const val ID_TRANSLATE = 3
        const val ID_AI = 4
    }
}
