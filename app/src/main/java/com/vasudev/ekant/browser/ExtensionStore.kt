package com.vasudev.ekant.browser

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/** Local-only store of user extensions (SharedPreferences + JSON, no cloud). */
class ExtensionStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aekant_extensions", Context.MODE_PRIVATE)

    fun getAll(): MutableList<Extension> {
        val raw = prefs.getString(KEY, null)
        val list = ArrayList<Extension>()
        if (!raw.isNullOrEmpty()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) list.add(Extension.fromJson(arr.getJSONObject(i)))
            } catch (_: Exception) {
                list.clear()
            }
        }
        if (list.isEmpty()) {
            list.addAll(defaults())
            save(list)
        } else {
            val have = list.map { it.id }.toHashSet()
            var added = false
            for (d in defaults()) if (!have.contains(d.id)) { list.add(d); added = true }
            if (added) save(list)
        }
        return list
    }

    fun getEnabled(): List<Extension> = getAll().filter { it.enabled && it.code.isNotBlank() }

    fun save(list: List<Extension>) {
        val arr = JSONArray()
        for (e in list) arr.put(e.toJson())
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun add(e: Extension) {
        val l = getAll(); l.add(e); save(l)
    }

    fun update(e: Extension) {
        val l = getAll()
        val i = l.indexOfFirst { it.id == e.id }
        if (i >= 0) { l[i] = e; save(l) }
    }

    fun delete(id: String) = save(getAll().filter { it.id != id })

    private fun defaults(): List<Extension> = listOf(
        Extension(
            "builtin-dark", "Dark mode (invert)", "*",
            "try{var s=document.createElement('style');s.id='aekant-dark';" +
                "s.textContent='html{filter:invert(1) hue-rotate(180deg);background:#101010!important}" +
                "img,video,picture,canvas,svg{filter:invert(1) hue-rotate(180deg)!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, true
        ),
        Extension(
            "builtin-font", "Bigger text", "*",
            "try{document.body.style.fontSize='18px';}catch(e){}",
            false, true
        ),
        Extension(
            "builtin-media", "Media sniffer", "*",
            "(function(){if(window.__aekantSniff){return;}window.__aekantSniff=1;" +
                "function r(u,t){try{if(u&&u.indexOf('blob:')!==0)window.AekantMedia&&window.AekantMedia.found(u,t);}catch(e){}}" +
                "function s(){try{" +
                "document.querySelectorAll('video,audio,source').forEach(function(el){r(el.currentSrc||el.src||el.getAttribute('src'),el.tagName.toLowerCase());});" +
                "document.querySelectorAll('a[href]').forEach(function(a){var h=a.getAttribute('href')||'';" +
                "if(/\\.(mp4|webm|m4a|mp3|ogg|m3u8|mpd|mov)(\\?|$)/i.test(h)){try{r(new URL(h,location.href).href,'link');}catch(e){}}});" +
                "}catch(e){}}" +
                "s();document.addEventListener('loadeddata',s,true);setInterval(s,5000);})();",
            true, true
        ),
        Extension(
            "builtin-bgplay", "Background playback (YouTube)", "*youtube.com*,*youtu.be*",
            "try{['hidden','webkitHidden'].forEach(function(k){try{Object.defineProperty(document,k,{get:function(){return false;},configurable:true});}catch(e){}});" +
                "['visibilityState','webkitVisibilityState'].forEach(function(k){try{Object.defineProperty(document,k,{get:function(){return 'visible';},configurable:true});}catch(e){}});" +
                "document.hasFocus=function(){return true;};" +
                "['visibilitychange','webkitvisibilitychange','blur','pagehide'].forEach(function(ev){" +
                "window.addEventListener(ev,function(e){e.stopImmediatePropagation();},true);" +
                "document.addEventListener(ev,function(e){e.stopImmediatePropagation();},true);});}catch(e){}",
            true, true
        ),
        Extension(
            "builtin-target", "Open links in same tab", "*",
            "try{document.querySelectorAll('a[target=_blank]').forEach(function(a){a.removeAttribute('target');});}catch(e){}",
            false, true
        )
    )

    companion object {
        private const val KEY = "extensions"
    }
}
