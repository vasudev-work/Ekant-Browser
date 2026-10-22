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
            false, true, desc = "Invert every page for comfortable night reading"
        ),
        Extension(
            "builtin-font", "Bigger text", "*",
            "try{document.body.style.fontSize='18px';}catch(e){}",
            false, true, desc = "Bump the default body text to 18px"
        ),
        Extension(
            "builtin-cookie", "Cookie banner remover", "*",
            "(function(){try{if(window.__aekantCookies)return;window.__aekantCookies=1;" +
                "var sel=['#onetrust-accept-btn-handler','.cc-allow','.cc-btn.cc-allow','#accept-cookies'," +
                "'.cookie-accept','#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll','.js-accept-cookies'];" +
                "function go(){try{for(var i=0;i<sel.length;i++){var e=document.querySelector(sel[i]);if(e){e.click();return;}}" +
                "var bs=document.querySelectorAll('button,a');" +
                "for(var j=0;j<bs.length;j++){var t=(bs[j].innerText||'').trim().toLowerCase();" +
                "if(/^(accept|accept all|agree|i agree|ok|got it|allow all|accept cookies)$/.test(t)){bs[j].click();return;}}}" +
                "catch(e){}}" +
                "go();setTimeout(go,1500);setTimeout(go,4000);}catch(e){}})();",
            true, true, desc = "Click the Accept button on common cookie banners"
        ),
        Extension(
            "builtin-json", "JSON viewer", "*",
            "(function(){try{" +
                "var ct=(document.contentType||'').toLowerCase();" +
                "var pre=document.body&&document.body.children.length===1&&document.body.children[0].tagName==='PRE';" +
                "if(ct.indexOf('json')<0&&!/\\.json(\\?|$)/i.test(location.href)&&!pre)return;" +
                "var t=document.body?document.body.innerText:'';" +
                "if(!t||t.length>3000000)return;" +
                "var o=JSON.parse(t);var pre=document.createElement('pre');" +
                "pre.style.cssText='white-space:pre-wrap;word-break:break-word;font:13px/1.5 monospace;padding:12px;margin:0';" +
                "pre.textContent=JSON.stringify(o,null,2);" +
                "document.body.style.margin='0';document.body.innerHTML='';document.body.appendChild(pre);" +
                "}catch(e){}})();",
            true, true, desc = "Pretty-print raw JSON responses"
        ),
        Extension(
            "builtin-ryd", "Return YouTube Dislike", "*youtube.com*",
            "(function(){try{var m=location.href.match(/[?&]v=([\\w-]{11})/);if(!m)return;var id=m[1];" +
                "fetch('https://returnyoutubedislikeapi.com/votes?videoId='+id).then(function(r){return r.json();})" +
                ".then(function(d){if(typeof d.dislikes!=='number')return;" +
                "var host=document.querySelector('#top-level-buttons-computed')||document.querySelector('#menu-container');" +
                "if(!host)return;var s=document.getElementById('aekant-dislikes');" +
                "if(!s){s=document.createElement('span');s.id='aekant-dislikes';" +
                "s.style.cssText='margin-left:10px;font-weight:600';host.appendChild(s);}" +
                "s.textContent='Dislikes: '+d.dislikes;}).catch(function(){});}catch(e){}})();",
            false, true, desc = "Show the dislike count again on YouTube"
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
            true, true, desc = "Detect video and audio files on the page"
        ),
        Extension(
            "builtin-bgplay", "Background playback (YouTube)", "*youtube.com*,*youtu.be*",
            "try{['hidden','webkitHidden'].forEach(function(k){try{Object.defineProperty(document,k,{get:function(){return false;},configurable:true});}catch(e){}});" +
                "['visibilityState','webkitVisibilityState'].forEach(function(k){try{Object.defineProperty(document,k,{get:function(){return 'visible';},configurable:true});}catch(e){}});" +
                "document.hasFocus=function(){return true;};" +
                "['visibilitychange','webkitvisibilitychange','blur','pagehide'].forEach(function(ev){" +
                "window.addEventListener(ev,function(e){e.stopImmediatePropagation();},true);" +
                "document.addEventListener(ev,function(e){e.stopImmediatePropagation();},true);});}catch(e){}",
            true, true, desc = "Keep YouTube playing when you leave the tab"
        ),
        Extension(
            "builtin-target", "Open links in same tab", "*",
            "try{document.querySelectorAll('a[target=_blank]').forEach(function(a){a.removeAttribute('target');});}catch(e){}",
            false, true, desc = "Rewrite target=\"_blank\" links to open in the same tab"
        )
    )

    /** Bundled catalogue shown under "Recommended"; installing copies it into the list. */
    fun catalog(): List<Extension> = listOf(
        Extension("cat-shorts", "Hide YouTube Shorts", "*youtube.com*",
            "try{var s=document.createElement('style');" +
                "s.textContent='ytd-reel-shelf-renderer,ytd-rich-shelf-renderer[is-shorts]{display:none!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Remove the Shorts shelves from YouTube"),
        Extension("cat-ytad", "Skip YouTube ads", "*youtube.com*",
            "(function(){setInterval(function(){try{var b=document.querySelector(" +
                "'.ytp-ad-skip-button,.ytp-skip-ad-button,.ytp-ad-skip-button-modern');if(b)b.click();}catch(e){}},1500);})();",
            false, false, "Click the Skip button as soon as it appears"),
        Extension("cat-noautoplay", "Stop video autoplay", "*",
            "(function(){setInterval(function(){try{document.querySelectorAll('video').forEach(function(v){" +
                "if(!v.paused&&v.currentTime<0.5){v.pause();}});}catch(e){}},2000);})();",
            false, false, "Pause videos that start playing by themselves"),
        Extension("cat-widegh", "Wide GitHub", "*github.com*",
            "try{var s=document.createElement('style');" +
                "s.textContent='.container-xl{max-width:100%!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Use the full window width on GitHub"),
        Extension("cat-sticky", "Unstick sticky headers", "*",
            "try{var s=document.createElement('style');" +
                "s.textContent='header{position:static!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Stop headers following you down the page"),
        Extension("cat-wiki", "Clean Wikipedia", "*wikipedia.org*",
            "try{var s=document.createElement('style');" +
                "s.textContent='.vector-column-start,.vector-column-end,#mw-panel{display:none!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Hide the sidebars on Wikipedia articles"),
        Extension("cat-read", "Bigger article text", "*",
            "try{var s=document.createElement('style');" +
                "s.textContent='article p,main p{font-size:18px!important;line-height:1.7!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Make article paragraphs larger and airier"),
        Extension("cat-gray", "Grayscale images", "*",
            "try{var s=document.createElement('style');s.textContent='img{filter:grayscale(1)!important}';" +
                "document.documentElement.appendChild(s);}catch(e){}",
            false, false, "Render every image in black and white")
    )

    companion object {
        private const val KEY = "extensions"
    }
}
